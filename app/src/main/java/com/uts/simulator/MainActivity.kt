package com.uts.simulator

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

// ============================================================================
// THEME COLORS
// ============================================================================
private val BgDark = Color(0xFF10131B)
private val BgSecondary = Color(0xFF191F2B)
private val CardBg = Color(0xFF222A38)
private val AccentGreen = Color(0xFF35D07F)
private val AccentBlue = Color(0xFF4C8DFF)
private val AccentGold = Color(0xFFFFC857)
private val AccentRed = Color(0xFFFF5A5F)
private val TextWhite = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFA5ADBA)

private val UtsColorScheme = darkColorScheme(
    primary = AccentGreen,
    secondary = AccentBlue,
    tertiary = AccentGold,
    background = BgDark,
    surface = BgSecondary,
    surfaceVariant = CardBg,
    onPrimary = Color.Black,
    onSecondary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextSecondary
)

// ============================================================================
// DOMAIN MODELS FOR COMPLETE SIMULATION & UI
// ============================================================================
private enum class MainTab(val title: String) {
    HOME("Home"),
    INTERNATIONAL("Intl"),
    CLUBS("Clubs"),
    SEASON("Season"),
    MORE("More")
}

private enum class SportType(
    val displayName: String,
    val icon: String,
    val baseScoreMin: Int,
    val baseScoreMax: Int,
    val allowsDraw: Boolean
) {
    FOOTBALL("Football", "⚽", 0, 5, true),
    BASKETBALL("Basketball", "🏀", 78, 128, false),
    TENNIS("Tennis (Sets)", "🎾", 0, 3, false),
    VOLLEYBALL("Volleyball (Sets)", "🏐", 0, 3, false),
    HANDBALL("Handball", "🤾", 20, 38, true),
    RUGBY("Rugby", "🏉", 10, 45, true),
    CRICKET("Cricket (Runs)", "🏏", 120, 240, false),
    ESPORTS("Esports (Maps)", "🎮", 0, 3, false),
    MMA("MMA (Rounds)", "🥊", 1, 5, false),
    BOXING("Boxing (Pts)", "🥊", 105, 120, true),
    TABLE_TENNIS("Table Tennis", "🏓", 0, 4, false),
    BASEBALL("Baseball", "⚾", 0, 12, false)
}

private data class UtsTeam(
    val id: String,
    val name: String,
    val code: String,
    val flagOrBadge: String,
    val confederation: String,
    val country: String,
    val isNational: Boolean,
    val category: String = "Men", // Men, Women, U-20, U-17, Club
    val divisionLevel: Int = 1,
    val overall: Int,
    val attack: Int,
    val midfield: Int,
    val defense: Int,
    val goalkeeper: Int,
    val stadium: String = "National Arena",
    val primaryColorHex: String = "#35D07F",
    val customLogoUri: String? = null,
    val previousOverall: Int = overall,
    val formStreak: Int = 0
)

private data class HistoricalWorldCup(
    val year: Int,
    val category: String, // "Men's WC", "Women's WC", "U-20 WC", "U-17 WC"
    val host: String,
    val champion: String,
    val runnerUp: String,
    val thirdPlace: String,
    val finalScore: String,
    val teamsCount: Int,
    val totalGoals: Int,
    val matchesPlayed: Int,
    val topScorer: String,
    val summaryGroups: List<String>
)

private data class ClubCompetitionSeason(
    val id: String,
    val competitionName: String,
    val confederation: String,
    val seasonYear: String,
    val format: String,
    val champion: String,
    val runnerUp: String,
    val finalScore: String,
    val venue: String,
    val topScorer: String,
    val notableClubs: List<String>,
    val verifiedStatus: String = "VERIFIED HISTORICAL"
)

private data class MatchEventItem(
    val minute: Int,
    val type: String, // GOAL, OWN_GOAL, PENALTY, YELLOW, RED, INJURY, SUB
    val teamName: String,
    val description: String
)

private data class SimulatedMatchResult(
    val id: String,
    val stageName: String,
    val homeTeam: UtsTeam,
    val awayTeam: UtsTeam,
    val homeScore: Int,
    val awayScore: Int,
    val extraTime: Boolean = false,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
    val homePossession: Int = 50,
    val homeShots: Int = 10,
    val awayShots: Int = 8,
    val homeShotsOnTarget: Int = 5,
    val awayShotsOnTarget: Int = 4,
    val homeCorners: Int = 5,
    val awayCorners: Int = 4,
    val homeFouls: Int = 11,
    val awayFouls: Int = 12,
    val homeYellows: Int = 1,
    val awayYellows: Int = 2,
    val homeReds: Int = 0,
    val awayReds: Int = 0,
    val events: List<MatchEventItem> = emptyList(),
    val sport: SportType = SportType.FOOTBALL
) {
    val winner: UtsTeam
        get() = when {
            homeScore > awayScore -> homeTeam
            awayScore > homeScore -> awayTeam
            homePenalties != null && awayPenalties != null ->
                if (homePenalties >= awayPenalties) homeTeam else awayTeam
            else -> if (homeTeam.overall >= awayTeam.overall) homeTeam else awayTeam
        }
}

private data class StandingRow(
    val team: UtsTeam,
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val points: Int = 0
) {
    val goalDiff: Int get() = goalsFor - goalsAgainst
}

private data class TournamentGroupState(
    val groupName: String,
    val teams: List<UtsTeam>,
    val matches: List<SimulatedMatchResult>,
    val standings: List<StandingRow>
)

private data class KnockoutRoundState(
    val roundName: String,
    val matches: List<SimulatedMatchResult>
)

private data class ActiveTournamentState(
    val id: String,
    val name: String,
    val type: String, // "WORLD_CUP", "CUSTOM_LEAGUE", "UNIVERSAL", "WHAT_IF", "SURVIVAL", "BATTLE_ROYALE"
    val sport: SportType,
    val hostName: String,
    val year: Int,
    val participants: List<UtsTeam>,
    val qualificationLogs: List<String> = emptyList(),
    val drawPots: List<List<UtsTeam>> = emptyList(),
    val groups: List<TournamentGroupState> = emptyList(),
    val knockoutRounds: List<KnockoutRoundState> = emptyList(),
    val champion: UtsTeam? = null,
    val runnerUp: UtsTeam? = null,
    val thirdPlace: UtsTeam? = null,
    val isCompleted: Boolean = false
)

private data class DivisionState(
    val divisionLevel: Int,
    val divisionName: String,
    val country: String,
    val promotedSlots: Int,
    val relegatedSlots: Int,
    val teams: List<UtsTeam>,
    val standings: List<StandingRow>,
    val matches: List<SimulatedMatchResult>,
    val currentMatchday: Int,
    val totalMatchdays: Int
)

private data class CountrySeasonSystem(
    val country: String,
    val confederation: String,
    val cupName: String,
    val divisions: List<DivisionState>,
    val cupWinner: UtsTeam? = null,
    val cupRunnerUp: UtsTeam? = null,
    val cupMatches: List<SimulatedMatchResult> = emptyList(),
    val championsLeagueQualifiers: List<UtsTeam> = emptyList(),
    val secondaryContinentalQualifiers: List<UtsTeam> = emptyList()
)

// ============================================================================
// MAIN ACTIVITY ENTRY POINT
// ============================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = UtsColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDark
                ) {
                    UniversalTournamentSimulatorApp()
                }
            }
        }
    }
}

// ============================================================================
// PERSISTENCE HELPER (JSON STORAGE IN FILESDIR)
// ============================================================================
private object LocalStorageManager {
    private const val TEAMS_FILE = "uts_custom_teams_v1.json"
    private const val SAVED_TOURNAMENTS_FILE = "uts_saved_tournaments_v1.json"

    fun saveTeams(context: Context, teams: List<UtsTeam>) {
        runCatching {
            val arr = JSONArray()
            teams.forEach { t ->
                val obj = JSONObject().apply {
                    put("id", t.id)
                    put("name", t.name)
                    put("code", t.code)
                    put("flagOrBadge", t.flagOrBadge)
                    put("confederation", t.confederation)
                    put("country", t.country)
                    put("isNational", t.isNational)
                    put("category", t.category)
                    put("divisionLevel", t.divisionLevel)
                    put("overall", t.overall)
                    put("attack", t.attack)
                    put("midfield", t.midfield)
                    put("defense", t.defense)
                    put("goalkeeper", t.goalkeeper)
                    put("stadium", t.stadium)
                    put("primaryColorHex", t.primaryColorHex)
                    put("customLogoUri", t.customLogoUri ?: "")
                    put("previousOverall", t.previousOverall)
                    put("formStreak", t.formStreak)
                }
                arr.put(obj)
            }
            File(context.filesDir, TEAMS_FILE).writeText(arr.toString())
        }
    }

    fun loadTeams(context: Context): List<UtsTeam>? {
        return runCatching {
            val file = File(context.filesDir, TEAMS_FILE)
            if (!file.exists()) return null
            val arr = JSONArray(file.readText())
            val list = mutableListOf<UtsTeam>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val uriStr = o.optString("customLogoUri", "")
                list.add(
                    UtsTeam(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        code = o.getString("code"),
                        flagOrBadge = o.getString("flagOrBadge"),
                        confederation = o.getString("confederation"),
                        country = o.getString("country"),
                        isNational = o.getBoolean("isNational"),
                        category = o.optString("category", "Men"),
                        divisionLevel = o.optInt("divisionLevel", 1),
                        overall = o.getInt("overall"),
                        attack = o.getInt("attack"),
                        midfield = o.getInt("midfield"),
                        defense = o.getInt("defense"),
                        goalkeeper = o.getInt("goalkeeper"),
                        stadium = o.optString("stadium", "National Stadium"),
                        primaryColorHex = o.optString("primaryColorHex", "#35D07F"),
                        customLogoUri = uriStr.ifBlank { null },
                        previousOverall = o.optInt("previousOverall", o.getInt("overall")),
                        formStreak = o.optInt("formStreak", 0)
                    )
                )
            }
            list.ifEmpty { null }
        }.getOrNull()
    }

    fun exportAllAsJsonString(teams: List<UtsTeam>, tournaments: List<ActiveTournamentState>): String {
        val root = JSONObject()
        root.put("app", "Universal Tournament Simulator")
        root.put("version", "2026.1")
        root.put("teamsCount", teams.size)
        root.put("savedTournamentsCount", tournaments.size)
        val tArr = JSONArray()
        tournaments.forEach { tr ->
            val o = JSONObject()
            o.put("id", tr.id)
            o.put("name", tr.name)
            o.put("type", tr.type)
            o.put("sport", tr.sport.name)
            o.put("year", tr.year)
            o.put("participantsCount", tr.participants.size)
            o.put("champion", tr.champion?.name ?: "In Progress")
            o.put("runnerUp", tr.runnerUp?.name ?: "-")
            tArr.put(o)
        }
        root.put("tournaments", tArr)
        return root.toString(2)
    }
}

// ============================================================================
// CORE SIMULATION ENGINE (MATCHES, STANDINGS, QUALIFIERS, DRAW, PROMOTION)
// ============================================================================
private object CoreMatchEngine {

    fun simulateMatch(
        stageName: String,
        home: UtsTeam,
        away: UtsTeam,
        mustHaveWinner: Boolean = false,
        neutralVenue: Boolean = false,
        randomnessFactor: Float = 1.0f,
        sport: SportType = SportType.FOOTBALL
    ): SimulatedMatchResult {
        if (sport != SportType.FOOTBALL) {
            return simulateMultiSportMatch(stageName, home, away, sport)
        }

        val homeAdv = if (neutralVenue) 0.0 else 3.0
        val ratingDiff = (home.overall + homeAdv + home.formStreak * 0.4) - (away.overall + away.formStreak * 0.4)
        val attVsDefHome = (home.attack - away.defense) * 0.025
        val attVsDefAway = (away.attack - home.defense) * 0.025

        val baseLambdaHome = (1.35 + (ratingDiff * 0.032) + attVsDefHome).coerceIn(0.20, 4.30)
        val baseLambdaAway = (1.15 - (ratingDiff * 0.028) + attVsDefAway).coerceIn(0.15, 4.00)

        var homeGoals = samplePoisson(baseLambdaHome, randomnessFactor)
        var awayGoals = samplePoisson(baseLambdaAway, randomnessFactor)

        val events = mutableListOf<MatchEventItem>()
        val goalMinutesHome = List(homeGoals) { Random.nextInt(2, 90) }.sorted()
        val goalMinutesAway = List(awayGoals) { Random.nextInt(2, 90) }.sorted()

        goalMinutesHome.forEach { min ->
            val isPen = Random.nextFloat() < 0.12f
            events.add(
                MatchEventItem(
                    minute = min,
                    type = if (isPen) "PENALTY" else "GOAL",
                    teamName = home.name,
                    description = if (isPen) "$min' PENALTY GOAL! ${home.name} convert from the spot!"
                    else "$min' GOAL! ${home.name} find the back of the net!"
                )
            )
        }
        goalMinutesAway.forEach { min ->
            val isOwn = Random.nextFloat() < 0.05f
            events.add(
                MatchEventItem(
                    minute = min,
                    type = if (isOwn) "OWN_GOAL" else "GOAL",
                    teamName = away.name,
                    description = if (isOwn) "$min' OWN GOAL! Unfortunate deflection gives ${away.name} a goal!"
                    else "$min' GOAL! ${away.name} score a clinical strike!"
                )
            )
        }

        val homeY = Random.nextInt(0, 4)
        val awayY = Random.nextInt(0, 4)
        repeat(homeY) {
            val m = Random.nextInt(10, 89)
            events.add(MatchEventItem(m, "YELLOW", home.name, "$m' Yellow card shown to ${home.name}."))
        }
        repeat(awayY) {
            val m = Random.nextInt(10, 89)
            events.add(MatchEventItem(m, "YELLOW", away.name, "$m' Yellow card shown to ${away.name}."))
        }

        val homeR = if (Random.nextFloat() < 0.05f) 1 else 0
        val awayR = if (Random.nextFloat() < 0.05f) 1 else 0
        if (homeR > 0) {
            val m = Random.nextInt(35, 88)
            events.add(MatchEventItem(m, "RED", home.name, "$m' RED CARD! ${home.name} reduced to 10 men!"))
        }
        if (awayR > 0) {
            val m = Random.nextInt(35, 88)
            events.add(MatchEventItem(m, "RED", away.name, "$m' RED CARD! ${away.name} player sent off!"))
        }

        var wentExtraTime = false
        var penH: Int? = null
        var penA: Int? = null

        if (mustHaveWinner && homeGoals == awayGoals) {
            wentExtraTime = true
            if (Random.nextFloat() < 0.28f) {
                homeGoals += 1
                val m = Random.nextInt(93, 119)
                events.add(MatchEventItem(m, "GOAL", home.name, "$m' EXTRA TIME GOAL! ${home.name} strike in extra time!"))
            }
            if (Random.nextFloat() < 0.25f) {
                awayGoals += 1
                val m = Random.nextInt(93, 119)
                events.add(MatchEventItem(m, "GOAL", away.name, "$m' EXTRA TIME GOAL! ${away.name} score in extra time!"))
            }
            if (homeGoals == awayGoals) {
                var hP = Random.nextInt(3, 6)
                var aP = Random.nextInt(3, 6)
                if (hP == aP) {
                    if (home.goalkeeper + home.overall >= away.goalkeeper + away.overall && Random.nextFloat() < 0.58f) hP++ else aP++
                }
                penH = hP
                penA = aP
                events.add(
                    MatchEventItem(
                        120,
                        "PENALTY",
                        if (hP > aP) home.name else away.name,
                        "120' PENALTY SHOOTOUT: ${home.name} $hP - $aP ${away.name}"
                    )
                )
            }
        }

        val possHome = (50 + (ratingDiff * 0.65).toInt() + Random.nextInt(-6, 7)).coerceIn(28, 72)
        val shotsH = max(homeGoals + 2, (possHome / 4) + Random.nextInt(0, 7))
        val shotsA = max(awayGoals + 2, ((100 - possHome) / 4) + Random.nextInt(0, 7))

        return SimulatedMatchResult(
            id = "M_${System.nanoTime()}_${Random.nextInt(1000, 9999)}",
            stageName = stageName,
            homeTeam = home,
            awayTeam = away,
            homeScore = homeGoals,
            awayScore = awayGoals,
            extraTime = wentExtraTime,
            homePenalties = penH,
            awayPenalties = penA,
            homePossession = possHome,
            homeShots = shotsH,
            awayShots = shotsA,
            homeShotsOnTarget = max(homeGoals, shotsH / 2),
            awayShotsOnTarget = max(awayGoals, shotsA / 2),
            homeCorners = Random.nextInt(2, 10),
            awayCorners = Random.nextInt(2, 10),
            homeFouls = Random.nextInt(7, 17),
            awayFouls = Random.nextInt(7, 17),
            homeYellows = homeY,
            awayYellows = awayY,
            homeReds = homeR,
            awayReds = awayR,
            events = events.sortedBy { it.minute },
            sport = SportType.FOOTBALL
        )
    }

    private fun simulateMultiSportMatch(
        stageName: String,
        home: UtsTeam,
        away: UtsTeam,
        sport: SportType
    ): SimulatedMatchResult {
        val diff = home.overall - away.overall
        var hScore = Random.nextInt(sport.baseScoreMin, sport.baseScoreMax + 1) + (diff / 6)
        var aScore = Random.nextInt(sport.baseScoreMin, sport.baseScoreMax + 1) - (diff / 6)
        hScore = max(sport.baseScoreMin, hScore)
        aScore = max(sport.baseScoreMin, aScore)
        if (!sport.allowsDraw && hScore == aScore) {
            if (diff >= 0) hScore++ else aScore++
        }
        return SimulatedMatchResult(
            id = "SP_${System.nanoTime()}",
            stageName = stageName,
            homeTeam = home,
            awayTeam = away,
            homeScore = hScore,
            awayScore = aScore,
            events = listOf(
                MatchEventItem(
                    1,
                    "RESULT",
                    home.name,
                    "${sport.displayName} Final: ${home.name} $hScore - $aScore ${away.name}"
                )
            ),
            sport = sport
        )
    }

    private fun samplePoisson(lambda: Double, randomness: Float): Int {
        val adjusted = (lambda * (0.85 + Random.nextDouble(0.0, 0.35 * randomness))).coerceAtLeast(0.1)
        val l = kotlin.math.exp(-adjusted)
        var k = 0
        var p = 1.0
        do {
            k++
            p *= Random.nextDouble()
        } while (p > l && k < 9)
        return k - 1
    }

    fun calculateStandings(teams: List<UtsTeam>, matches: List<SimulatedMatchResult>, winPts: Int = 3, drawPts: Int = 1): List<StandingRow> {
        val map = teams.associate { it.id to StandingRow(team = it) }.toMutableMap()
        matches.forEach { m ->
            val h = map[m.homeTeam.id] ?: StandingRow(m.homeTeam)
            val a = map[m.awayTeam.id] ?: StandingRow(m.awayTeam)
            when {
                m.homeScore > m.awayScore -> {
                    map[m.homeTeam.id] = h.copy(
                        played = h.played + 1,
                        won = h.won + 1,
                        goalsFor = h.goalsFor + m.homeScore,
                        goalsAgainst = h.goalsAgainst + m.awayScore,
                        points = h.points + winPts
                    )
                    map[m.awayTeam.id] = a.copy(
                        played = a.played + 1,
                        lost = a.lost + 1,
                        goalsFor = a.goalsFor + m.awayScore,
                        goalsAgainst = a.goalsAgainst + m.homeScore
                    )
                }
                m.awayScore > m.homeScore -> {
                    map[m.awayTeam.id] = a.copy(
                        played = a.played + 1,
                        won = a.won + 1,
                        goalsFor = a.goalsFor + m.awayScore,
                        goalsAgainst = a.goalsAgainst + m.homeScore,
                        points = a.points + winPts
                    )
                    map[m.homeTeam.id] = h.copy(
                        played = h.played + 1,
                        lost = h.lost + 1,
                        goalsFor = h.goalsFor + m.homeScore,
                        goalsAgainst = h.goalsAgainst + m.awayScore
                    )
                }
                else -> {
                    map[m.homeTeam.id] = h.copy(
                        played = h.played + 1,
                        drawn = h.drawn + 1,
                        goalsFor = h.goalsFor + m.homeScore,
                        goalsAgainst = h.goalsAgainst + m.awayScore,
                        points = h.points + drawPts
                    )
                    map[m.awayTeam.id] = a.copy(
                        played = a.played + 1,
                        drawn = a.drawn + 1,
                        goalsFor = a.goalsFor + m.awayScore,
                        goalsAgainst = a.goalsAgainst + m.homeScore,
                        points = a.points + drawPts
                    )
                }
            }
        }
        return map.values.sortedWith(
            compareByDescending<StandingRow> { it.points }
                .thenByDescending { it.goalDiff }
                .thenByDescending { it.goalsFor }
                .thenByDescending { it.team.overall }
        )
    }

    fun applyDynamicRatingUpdate(team: UtsTeam, opponent: UtsTeam, goalsFor: Int, goalsAgainst: Int): UtsTeam {
        val diff = opponent.overall - team.overall
        val delta = when {
            goalsFor > goalsAgainst -> if (diff >= 10) 2 else if (diff >= 0) 1 else 0
            goalsFor < goalsAgainst -> if (diff <= -10) -2 else if (diff <= 0) -1 else 0
            else -> if (diff >= 12) 1 else 0
        }
        val newStreak = when {
            goalsFor > goalsAgainst -> if (team.formStreak >= 0) team.formStreak + 1 else 1
            goalsFor < goalsAgainst -> if (team.formStreak <= 0) team.formStreak - 1 else -1
            else -> 0
        }
        val newOvr = (team.overall + delta).coerceIn(15, 99)
        return team.copy(
            previousOverall = team.overall,
            overall = newOvr,
            attack = (team.attack + delta).coerceIn(15, 99),
            defense = (team.defense + delta).coerceIn(15, 99),
            formStreak = newStreak
        )
    }
}
// ============================================================================
// MASTER VERIFIED DATASET (ALL 6 CONFEDERATIONS, WORLD CUPS, CLUBS & LEAGUES)
// ============================================================================
private object MasterSeedData {

    fun buildDefaultTeams(): List<UtsTeam> {
        val list = mutableListOf<UtsTeam>()

        fun addNat(code: String, name: String, flag: String, conf: String, ovr: Int) {
            list.add(
                UtsTeam(
                    id = "NAT_M_$code",
                    name = name,
                    code = code,
                    flagOrBadge = flag,
                    confederation = conf,
                    country = name,
                    isNational = true,
                    category = "Men",
                    overall = ovr,
                    attack = (ovr + Random.nextInt(-2, 3)).coerceIn(20, 99),
                    midfield = (ovr + Random.nextInt(-2, 3)).coerceIn(20, 99),
                    defense = (ovr + Random.nextInt(-2, 3)).coerceIn(20, 99),
                    goalkeeper = (ovr + Random.nextInt(-3, 3)).coerceIn(20, 99),
                    stadium = "$name National Stadium"
                )
            )
        }

        // UEFA (55 Member Associations)
        val uefa = listOf(
            Triple("ESP", "Spain", "🇪🇸") to 95, Triple("FRA", "France", "🇫🇷") to 94,
            Triple("ENG", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿") to 93, Triple("GER", "Germany", "🇩🇪") to 92,
            Triple("POR", "Portugal", "🇵🇹") to 91, Triple("NED", "Netherlands", "🇳🇱") to 90,
            Triple("ITA", "Italy", "🇮🇹") to 89, Triple("BEL", "Belgium", "🇧🇪") to 87,
            Triple("CRO", "Croatia", "🇭🇷") to 86, Triple("SUI", "Switzerland", "🇨🇭") to 84,
            Triple("DEN", "Denmark", "🇩🇰") to 83, Triple("AUT", "Austria", "🇦🇹") to 83,
            Triple("TUR", "Turkey", "🇹🇷") to 82, Triple("UKR", "Ukraine", "🇺🇦") to 81,
            Triple("SRB", "Serbia", "🇷🇸") to 80, Triple("POL", "Poland", "🇵🇱") to 80,
            Triple("SWE", "Sweden", "🇸🇪") to 79, Triple("NOR", "Norway", "🇳🇴") to 81,
            Triple("SCO", "Scotland", "🏴󠁧󠁢󠁳󠁣󠁴󠁿") to 78, Triple("CZE", "Czechia", "🇨🇿") to 79,
            Triple("HUN", "Hungary", "🇭🇺") to 78, Triple("GRE", "Greece", "🇬🇷") to 78,
            Triple("ROU", "Romania", "🇷🇴") to 77, Triple("SVK", "Slovakia", "🇸🇰") to 77,
            Triple("SVN", "Slovenia", "🇸🇮") to 77, Triple("GEO", "Georgia", "🇬🇪") to 76,
            Triple("ALB", "Albania", "🇦🇱") to 75, Triple("WAL", "Wales", "🏴󠁧󠁢󠁷󠁬󠁳󠁿") to 76,
            Triple("IRL", "Republic of Ireland", "🇮🇪") to 74, Triple("FIN", "Finland", "🇫🇮") to 73,
            Triple("BIH", "Bosnia & Herzegovina", "🇧🇦") to 73, Triple("ISL", "Iceland", "🇮🇸") to 72,
            Triple("MKD", "North Macedonia", "🇲🇰") to 72, Triple("MNE", "Montenegro", "🇲🇪") to 72,
            Triple("NIR", "Northern Ireland", "🇬🇧") to 71, Triple("ISR", "Israel", "🇮🇱") to 73,
            Triple("BUL", "Bulgaria", "🇧🇬") to 70, Triple("KVX", "Kosovo", "🇽🇰") to 71,
            Triple("ARM", "Armenia", "🇦🇲") to 69, Triple("LUX", "Luxembourg", "🇱🇺") to 68,
            Triple("AZE", "Azerbaijan", "🇦🇿") to 67, Triple("KAZ", "Kazakhstan", "🇰🇿") to 67,
            Triple("CYP", "Cyprus", "🇨🇾") to 65, Triple("EST", "Estonia", "🇪🇪") to 64,
            Triple("BLR", "Belarus", "🇧🇾") to 65, Triple("LTU", "Lithuania", "🇱🇹") to 63,
            Triple("LVA", "Latvia", "🇱🇻") to 63, Triple("FRO", "Faroe Islands", "🇫🇴") to 62,
            Triple("MDA", "Moldova", "🇲🇩") to 63, Triple("MLT", "Malta", "🇲🇹") to 60,
            Triple("AND", "Andorra", "🇦🇩") to 56, Triple("GIB", "Gibraltar", "🇬🇮") to 54,
            Triple("LIE", "Liechtenstein", "🇱🇮") to 53, Triple("SMR", "San Marino", "🇸🇲") to 48,
            Triple("RUS", "Russia", "🇷🇺") to 79
        )
        uefa.forEach { (info, ovr) -> addNat(info.first, info.second, info.third, "UEFA", ovr) }

        // CONMEBOL (10 Member Associations)
        val conmebol = listOf(
            Triple("ARG", "Argentina", "🇦🇷") to 96, Triple("BRA", "Brazil", "🇧🇷") to 93,
            Triple("URU", "Uruguay", "🇺🇾") to 89, Triple("COL", "Colombia", "🇨🇴") to 88,
            Triple("ECU", "Ecuador", "🇪🇨") to 83, Triple("VEN", "Venezuela", "🇻🇪") to 80,
            Triple("PAR", "Paraguay", "🇵🇾") to 79, Triple("CHL", "Chile", "🇨🇱") to 78,
            Triple("PER", "Peru", "🇵🇪") to 77, Triple("BOL", "Bolivia", "🇧🇴") to 74
        )
        conmebol.forEach { (info, ovr) -> addNat(info.first, info.second, info.third, "CONMEBOL", ovr) }

        // CAF (54 Member Associations)
        val caf = listOf(
            Triple("MAR", "Morocco", "🇲🇦") to 88, Triple("SEN", "Senegal", "🇸🇳") to 86,
            Triple("ALG", "Algeria", "🇩🇿") to 85, Triple("EGY", "Egypt", "🇪🇬") to 84,
            Triple("NGA", "Nigeria", "🇳🇬") to 84, Triple("CIV", "Côte d'Ivoire", "🇨🇮") to 84,
            Triple("TUN", "Tunisia", "🇹🇳") to 81, Triple("CMR", "Cameroon", "🇨🇲") to 81,
            Triple("MLI", "Mali", "🇲🇱") to 80, Triple("RSA", "South Africa", "🇿🇦") to 79,
            Triple("GHA", "Ghana", "🇬🇭") to 79, Triple("COD", "DR Congo", "🇨🇩") to 79,
            Triple("BFA", "Burkina Faso", "🇧🇫") to 77, Triple("CPV", "Cape Verde", "🇨🇻") to 76,
            Triple("GIN", "Guinea", "🇬🇳") to 76, Triple("GNQ", "Equatorial Guinea", "🇬🇶") to 75,
            Triple("ANG", "Angola", "🇦🇴") to 76, Triple("GAB", "Gabon", "🇬🇦") to 74,
            Triple("ZAM", "Zambia", "🇿🇲") to 74, Triple("BEN", "Benin", "🇧🇯") to 72,
            Triple("UGA", "Uganda", "🇺🇬") to 72, Triple("MOZ", "Mozambique", "🇲🇿") to 72,
            Triple("MTN", "Mauritania", "🇲🇷") to 71, Triple("NAM", "Namibia", "🇳🇦") to 70,
            Triple("KEN", "Kenya", "🇰🇪") to 70, Triple("GAM", "Gambia", "🇬🇲") to 70,
            Triple("MAD", "Madagascar", "🇲🇬") to 69, Triple("TOG", "Togo", "🇹🇬") to 69,
            Triple("TAN", "Tanzania", "🇹🇿") to 70, Triple("ZIM", "Zimbabwe", "🇿🇼") to 69,
            Triple("LBY", "Libya", "🇱🇾") to 69, Triple("SDN", "Sudan", "🇸🇩") to 69,
            Triple("CGO", "Congo", "🇨🇬") to 68, Triple("COM", "Comoros", "🇰🇲") to 70,
            Triple("MWI", "Malawi", "🇲🇼") to 67, Triple("SLE", "Sierra Leone", "🇸🇱") to 67,
            Triple("GNB", "Guinea-Bissau", "🇬🇼") to 68, Triple("BOT", "Botswana", "🇧🇼") to 66,
            Triple("RWA", "Rwanda", "🇷🇼") to 67, Triple("BDI", "Burundi", "🇧🇮") to 65,
            Triple("ETH", "Ethiopia", "🇪🇹") to 65, Triple("LBR", "Liberia", "🇱🇷") to 64,
            Triple("NIG", "Niger", "🇳🇪") to 65, Triple("CTA", "Central African Rep.", "🇨🇫") to 64,
            Triple("LES", "Lesotho", "🇱🇸") to 62, Triple("SWZ", "Eswatini", "🇸🇿") to 61,
            Triple("SSD", "South Sudan", "🇸🇸") to 61, Triple("MRI", "Mauritius", "🇲🇺") to 59,
            Triple("CHA", "Chad", "🇹🇩") to 59, Triple("STP", "São Tomé & Príncipe", "🇸🇹") to 57,
            Triple("DJI", "Djibouti", "🇩🇯") to 55, Triple("SOM", "Somalia", "🇸🇴") to 53,
            Triple("SEY", "Seychelles", "🇸🇨") to 52, Triple("ERI", "Eritrea", "🇪🇷") to 54
        )
        caf.forEach { (info, ovr) -> addNat(info.first, info.second, info.third, "CAF", ovr) }

        // AFC (47 Member Associations)
        val afc = listOf(
            Triple("JPN", "Japan", "🇯🇵") to 86, Triple("IRN", "Iran", "🇮🇷") to 83,
            Triple("KOR", "South Korea", "🇰🇷") to 84, Triple("AUS", "Australia", "🇦🇺") to 82,
            Triple("QAT", "Qatar", "🇶🇦") to 79, Triple("KSA", "Saudi Arabia", "🇸🇦") to 80,
            Triple("IRQ", "Iraq", "🇮🇶") to 78, Triple("UZB", "Uzbekistan", "🇺🇿") to 79,
            Triple("JOR", "Jordan", "🇯🇴") to 78, Triple("UAE", "United Arab Emirates", "🇦🇪") to 76,
            Triple("OMA", "Oman", "🇴🇲") to 74, Triple("BHR", "Bahrain", "🇧🇭") to 74,
            Triple("CHN", "China PR", "🇨🇳") to 72, Triple("SYR", "Syria", "🇸🇾") to 72,
            Triple("PLE", "Palestine", "🇵🇸") to 73, Triple("THA", "Thailand", "🇹🇭") to 71,
            Triple("KGZ", "Kyrgyzstan", "🇰🇬") to 70, Triple("TJK", "Tajikistan", "🇹🇯") to 70,
            Triple("VIE", "Vietnam", "🇻🇳") to 70, Triple("PRK", "North Korea", "🇰🇵") to 70,
            Triple("LBN", "Lebanon", "🇱🇧") to 69, Triple("IND", "India", "🇮🇳") to 68,
            Triple("IDN", "Indonesia", "🇮🇩") to 71, Triple("MAS", "Malaysia", "🇲🇾") to 68,
            Triple("KUW", "Kuwait", "🇰🇼") to 67, Triple("TKM", "Turkmenistan", "🇹🇲") to 66,
            Triple("PHI", "Philippines", "🇵🇭") to 65, Triple("AFG", "Afghanistan", "🇦🇫") to 63,
            Triple("HKG", "Hong Kong", "🇭🇰") to 64, Triple("SGP", "Singapore", "🇸🇬") to 63,
            Triple("YEM", "Yemen", "🇾🇪") to 63, Triple("MYA", "Myanmar", "🇲🇲") to 61,
            Triple("TPE", "Chinese Taipei", "🇹🇼") to 60, Triple("MDV", "Maldives", "🇲🇻") to 59,
            Triple("NEP", "Nepal", "🇳🇵") to 58, Triple("CAM", "Cambodia", "🇰🇭") to 58,
            Triple("BAN", "Bangladesh", "🇧🇩") to 57, Triple("PAK", "Pakistan", "🇵🇰") to 56,
            Triple("LAO", "Laos", "🇱🇦") to 55, Triple("MNG", "Mongolia", "🇲🇳") to 55,
            Triple("BHU", "Bhutan", "🇧🇹") to 54, Triple("BRU", "Brunei", "🇧🇳") to 53,
            Triple("MAC", "Macau", "🇲🇴") to 52, Triple("TLS", "Timor-Leste", "🇹🇱") to 52,
            Triple("SRI", "Sri Lanka", "🇱🇰") to 53, Triple("GUM", "Guam", "🇬🇺") to 51
        )
        afc.forEach { (info, ovr) -> addNat(info.first, info.second, info.third, "AFC", ovr) }

        // CONCACAF (35 FIFA Associations)
        val concacaf = listOf(
            Triple("USA", "United States", "🇺🇸") to 84, Triple("MEX", "Mexico", "🇲🇽") to 84,
            Triple("CAN", "Canada", "🇨🇦") to 82, Triple("PAN", "Panama", "🇵🇦") to 78,
            Triple("CRC", "Costa Rica", "🇨🇷") to 77, Triple("JAM", "Jamaica", "🇯🇲") to 76,
            Triple("HON", "Honduras", "🇭🇳") to 74, Triple("SLV", "El Salvador", "🇸🇻") to 71,
            Triple("HAI", "Haiti", "🇭🇹") to 71, Triple("CUR", "Curaçao", "🇨🇼") to 71,
            Triple("TRI", "Trinidad & Tobago", "🇹🇹") to 70, Triple("GUA", "Guatemala", "🇬🇹") to 72,
            Triple("SUR", "Suriname", "🇸🇷") to 70, Triple("NIC", "Nicaragua", "🇳🇮") to 67,
            Triple("CUB", "Cuba", "🇨🇺") to 65, Triple("DOM", "Dominican Republic", "🇩🇴") to 66,
            Triple("GUY", "Guyana", "🇬🇾") to 64, Triple("PUR", "Puerto Rico", "🇵🇷") to 63,
            Triple("ATG", "Antigua & Barbuda", "🇦🇬") to 61, Triple("SKN", "St. Kitts & Nevis", "🇰🇳") to 61,
            Triple("LCA", "Saint Lucia", "🇱🇨") to 60, Triple("GRN", "Grenada", "🇬🇩") to 60,
            Triple("BRB", "Barbados", "🇧🇧") to 59, Triple("BLZ", "Belize", "🇧🇿") to 58,
            Triple("BER", "Bermuda", "🇧🇲") to 59, Triple("VIN", "St. Vincent", "🇻🇨") to 59,
            Triple("DMA", "Dominica", "🇩🇲") to 57, Triple("MSR", "Montserrat", "🇲🇸") to 57,
            Triple("BAH", "Bahamas", "🇧🇸") to 54, Triple("ARU", "Aruba", "🇦🇼") to 55,
            Triple("CAY", "Cayman Islands", "🇰🇾") to 53, Triple("TCA", "Turks & Caicos", "🇹🇨") to 51,
            Triple("VGB", "British Virgin Islands", "🇻🇬") to 50, Triple("VIR", "US Virgin Islands", "🇻🇮") to 50,
            Triple("AIA", "Anguilla", "🇦🇮") to 49
        )
        concacaf.forEach { (info, ovr) -> addNat(info.first, info.second, info.third, "CONCACAF", ovr) }

        // OFC (11 Member Associations)
        val ofc = listOf(
            Triple("NZL", "New Zealand", "🇳🇿") to 76, Triple("SOL", "Solomon Islands", "🇸🇧") to 64,
            Triple("FIJ", "Fiji", "🇫🇯") to 63, Triple("NCL", "New Caledonia", "🇳🇨") to 63,
            Triple("TAH", "Tahiti", "🇵🇫") to 63, Triple("VAN", "Vanuatu", "🇻🇺") to 61,
            Triple("PNG", "Papua New Guinea", "🇵🇬") to 61, Triple("SAM", "Samoa", "🇼🇸") to 55,
            Triple("COK", "Cook Islands", "🇨🇰") to 53, Triple("TGA", "Tonga", "🇹🇴") to 51,
            Triple("ASA", "American Samoa", "🇦🇸") to 49
        )
        ofc.forEach { (info, ovr) -> addNat(info.first, info.second, info.third, "OFC", ovr) }

        // WOMEN'S & YOUTH NATIONAL TEAMS SAMPLE
        val womensTop = listOf(
            Triple("USA_W", "USA (Women)", "🇺🇸") to 95,
            Triple("ESP_W", "Spain (Women)", "🇪🇸") to 95,
            Triple("ENG_W", "England (Women)", "🏴󠁧󠁢󠁥󠁮󠁧󠁿") to 93,
            Triple("GER_W", "Germany (Women)", "🇩🇪") to 92,
            Triple("FRA_W", "France (Women)", "🇫🇷") to 91,
            Triple("SWE_W", "Sweden (Women)", "🇸🇪") to 90,
            Triple("JPN_W", "Japan (Women)", "🇯🇵") to 90,
            Triple("BRA_W", "Brazil (Women)", "🇧🇷") to 89,
            Triple("CAN_W", "Canada (Women)", "🇨🇦") to 88,
            Triple("AUS_W", "Australia (Women)", "🇦🇺") to 88,
            Triple("NED_W", "Netherlands (Women)", "🇳🇱") to 87,
            Triple("NOR_W", "Norway (Women)", "🇳🇴") to 86
        )
        womensTop.forEach { (info, ovr) ->
            list.add(
                UtsTeam(
                    id = "NAT_W_${info.first}",
                    name = info.second,
                    code = info.first,
                    flagOrBadge = info.third,
                    confederation = "FIFA-W",
                    country = info.second,
                    isNational = true,
                    category = "Women",
                    overall = ovr,
                    attack = ovr,
                    midfield = ovr - 1,
                    defense = ovr - 1,
                    goalkeeper = ovr - 2
                )
            )
        }

        // REAL MEN'S CLUB DATABASE (MULTI-DIVISION PYRAMIDS ACROSS EUROPE, AFRICA, AMERICAS, ASIA)
        fun addClub(id: String, name: String, country: String, conf: String, div: Int, ovr: Int, badge: String = "🛡️") {
            list.add(
                UtsTeam(
                    id = "CLUB_$id",
                    name = name,
                    code = id.take(3).uppercase(),
                    flagOrBadge = badge,
                    confederation = conf,
                    country = country,
                    isNational = false,
                    category = "Club",
                    divisionLevel = div,
                    overall = ovr,
                    attack = (ovr + Random.nextInt(-2, 3)).coerceIn(30, 99),
                    midfield = (ovr + Random.nextInt(-2, 3)).coerceIn(30, 99),
                    defense = (ovr + Random.nextInt(-2, 3)).coerceIn(30, 99),
                    goalkeeper = (ovr + Random.nextInt(-2, 3)).coerceIn(30, 99),
                    stadium = "$name Arena"
                )
            )
        }

        // ENGLAND (Div 1 Premier League, Div 2 Championship, Div 3 League One)
        val engD1 = listOf(
            "MCI" to ("Manchester City" to 95), "ARS" to ("Arsenal" to 92), "LIV" to ("Liverpool" to 93),
            "CHE" to ("Chelsea" to 88), "AVL" to ("Aston Villa" to 86), "NEW" to ("Newcastle United" to 86),
            "TOT" to ("Tottenham Hotspur" to 86), "MUN" to ("Manchester United" to 85), "BHA" to ("Brighton" to 83),
            "WHU" to ("West Ham United" to 82), "CRY" to ("Crystal Palace" to 81), "FUL" to ("Fulham" to 81),
            "BOU" to ("Bournemouth" to 80), "WOL" to ("Wolverhampton" to 79), "EVE" to ("Everton" to 79),
            "BRE" to ("Brentford" to 80), "NFO" to ("Nottingham Forest" to 81), "LEI" to ("Leicester City" to 78)
        )
        engD1.forEach { (c, p) -> addClub(c, p.first, "England", "UEFA", 1, p.second) }

        val engD2 = listOf(
            "LEE" to ("Leeds United" to 78), "BUR" to ("Burnley" to 77), "SHU" to ("Sheffield United" to 76),
            "SUN" to ("Sunderland" to 76), "MID" to ("Middlesbrough" to 75), "COV" to ("Coventry City" to 75),
            "WBA" to ("West Bromwich" to 75), "NOR" to ("Norwich City" to 74), "WAT" to ("Watford" to 73),
            "IPS" to ("Ipswich Town" to 77), "SOU" to ("Southampton" to 77), "LUT" to ("Luton Town" to 74)
        )
        engD2.forEach { (c, p) -> addClub(c, p.first, "England", "UEFA", 2, p.second) }

        val engD3 = listOf(
            "BIR" to ("Birmingham City" to 73), "WRE" to ("Wrexham" to 71), "BOL" to ("Bolton Wanderers" to 70),
            "HUD" to ("Huddersfield Town" to 70), "CHA" to ("Charlton Athletic" to 69), "WIG" to ("Wigan Athletic" to 69),
            "REA" to ("Reading" to 68), "BAR" to ("Barnsley" to 69)
        )
        engD3.forEach { (c, p) -> addClub(c, p.first, "England", "UEFA", 3, p.second) }

        // SPAIN (La Liga & Segunda)
        val espD1 = listOf(
            "RMA" to ("Real Madrid" to 96), "FCB" to ("Barcelona" to 93), "ATM" to ("Atlético Madrid" to 90),
            "ATH" to ("Athletic Club" to 85), "RSO" to ("Real Sociedad" to 84), "VIL" to ("Villarreal" to 84),
            "BET" to ("Real Betis" to 83), "GIR" to ("Girona" to 82), "SEV" to ("Sevilla" to 81),
            "VAL" to ("Valencia" to 80), "CEL" to ("Celta Vigo" to 79), "OSA" to ("Osasuna" to 79)
        )
        espD1.forEach { (c, p) -> addClub(c, p.first, "Spain", "UEFA", 1, p.second) }

        val espD2 = listOf(
            "ESP2" to ("Espanyol" to 77), "ZAR" to ("Real Zaragoza" to 74), "SPO" to ("Sporting Gijón" to 74),
            "RAC" to ("Racing Santander" to 75), "LEV" to ("Levante" to 75), "ELC" to ("Elche" to 74),
            "ALM" to ("Almería" to 75), "GRA" to ("Granada" to 75)
        )
        espD2.forEach { (c, p) -> addClub(c, p.first, "Spain", "UEFA", 2, p.second) }

        // GERMANY (Bundesliga & 2. Bundesliga)
        val gerD1 = listOf(
            "BAY" to ("Bayern Munich" to 94), "B04" to ("Bayer Leverkusen" to 91), "BVB" to ("Borussia Dortmund" to 89),
            "RBL" to ("RB Leipzig" to 87), "VFB" to ("VfB Stuttgart" to 85), "SGE" to ("Eintracht Frankfurt" to 84),
            "SCF" to ("SC Freiburg" to 82), "WOB" to ("VfL Wolfsburg" to 81), "BMG" to ("Borussia M'gladbach" to 80),
            "SVW" to ("Werder Bremen" to 80)
        )
        gerD1.forEach { (c, p) -> addClub(c, p.first, "Germany", "UEFA", 1, p.second) }

        val gerD2 = listOf(
            "HSV" to ("Hamburger SV" to 76), "KOE" to ("1. FC Köln" to 77), "S04" to ("Schalke 04" to 74),
            "BSC" to ("Hertha BSC" to 75), "F95" to ("Fortuna Düsseldorf" to 75), "H96" to ("Hannover 96" to 74)
        )
        gerD2.forEach { (c, p) -> addClub(c, p.first, "Germany", "UEFA", 2, p.second) }

        // ITALY (Serie A & Serie B)
        val itaD1 = listOf(
            "INT" to ("Inter Milan" to 93), "JUV" to ("Juventus" to 89), "MIL" to ("AC Milan" to 89),
            "NAP" to ("Napoli" to 89), "ATA" to ("Atalanta" to 88), "ROM" to ("AS Roma" to 85),
            "LAZ" to ("Lazio" to 84), "FIO" to ("Fiorentina" to 83), "BOL_I" to ("Bologna" to 82),
            "TOR" to ("Torino" to 80)
        )
        itaD1.forEach { (c, p) -> addClub(c, p.first, "Italy", "UEFA", 1, p.second) }

        val itaD2 = listOf(
            "PAR_I" to ("Parma" to 76), "COM_I" to ("Como" to 76), "PAL" to ("Palermo" to 74),
            "SAM_I" to ("Sampdoria" to 74), "BAR_I" to ("Bari" to 73), "CRE" to ("Cremonese" to 74)
        )
        itaD2.forEach { (c, p) -> addClub(c, p.first, "Italy", "UEFA", 2, p.second) }

        // FRANCE (Ligue 1 & Ligue 2)
        val fraD1 = listOf(
            "PSG" to ("Paris Saint-Germain" to 92), "ASM" to ("AS Monaco" to 85), "OM" to ("Olympique Marseille" to 85),
            "LIL" to ("LOSC Lille" to 84), "OL" to ("Olympique Lyonnais" to 83), "OGCN" to ("OGC Nice" to 83),
            "RCL" to ("RC Lens" to 82), "SRFC" to ("Stade Rennais" to 81), "SB29" to ("Stade Brestois" to 81),
            "FCN" to ("FC Nantes" to 78)
        )
        fraD1.forEach { (c, p) -> addClub(c, p.first, "France", "UEFA", 1, p.second) }

        val fraD2 = listOf(
            "ASSE" to ("AS Saint-Étienne" to 76), "FCM" to ("FC Metz" to 75), "FCL" to ("FC Lorient" to 75),
            "PFC" to ("Paris FC" to 75), "SMC" to ("SM Caen" to 73), "SCB" to ("SC Bastia" to 73)
        )
        fraD2.forEach { (c, p) -> addClub(c, p.first, "France", "UEFA", 2, p.second) }

        // ALGERIA (Ligue 1 Mobilis & Ligue 2)
        val algD1 = listOf(
            "MCA" to ("MC Alger" to 80), "CRB" to ("CR Belouizdad" to 80), "CSC" to ("CS Constantine" to 79),
            "USMA" to ("USM Alger" to 79), "JSK" to ("JS Kabylie" to 78), "ESS" to ("ES Sétif" to 78),
            "MCO" to ("MC Oran" to 76), "PAC" to ("Paradou AC" to 76), "ASO" to ("ASO Chlef" to 75),
            "JSMS" to ("JS Saoura" to 75), "USMK" to ("USM Khenchela" to 74), "NCB" to ("NC Magra" to 73)
        )
        algD1.forEach { (c, p) -> addClub(c, p.first, "Algeria", "CAF", 1, p.second) }

        val algD2 = listOf(
            "NAHD" to ("NA Hussein Dey" to 72), "RCK" to ("RC Kouba" to 72), "USMH" to ("USM El Harrach" to 73),
            "JSMB" to ("JSM Béjaïa" to 71), "MOB" to ("MO Béjaïa" to 71), "AAS" to ("Amel Aïn Sefra" to 70),
            "WAT_A" to ("WA Tlemcen" to 71), "USMB" to ("USM Blida" to 71)
        )
        algD2.forEach { (c, p) -> addClub(c, p.first, "Algeria", "CAF", 2, p.second) }

        // REST OF AFRICA, SOUTH AMERICA, ASIA, NORTH AMERICA, EUROPE GIANTS
        val globalClubs = listOf(
            Triple("AHL", "Al Ahly SC", "Egypt") to ("CAF" to 84),
            Triple("ZAM_C", "Zamalek SC", "Egypt") to ("CAF" to 80),
            Triple("PYR", "Pyramids FC", "Egypt") to ("CAF" to 80),
            Triple("WAC", "Wydad AC", "Morocco") to ("CAF" to 81),
            Triple("RCA", "Raja CA", "Morocco") to ("CAF" to 81),
            Triple("RSB", "RS Berkane", "Morocco") to ("CAF" to 80),
            Triple("EST_T", "Espérance de Tunis", "Tunisia") to ("CAF" to 82),
            Triple("SUN_C", "Mamelodi Sundowns", "South Africa") to ("CAF" to 82),
            Triple("TPM", "TP Mazembe", "DR Congo") to ("CAF" to 78),
            Triple("PAL_B", "Palmeiras", "Brazil") to ("CONMEBOL" to 85),
            Triple("FLA", "Flamengo", "Brazil") to ("CONMEBOL" to 86),
            Triple("BOT_B", "Botafogo", "Brazil") to ("CONMEBOL" to 85),
            Triple("SAO", "São Paulo", "Brazil") to ("CONMEBOL" to 82),
            Triple("COR", "Corinthians", "Brazil") to ("CONMEBOL" to 81),
            Triple("RIV", "River Plate", "Argentina") to ("CONMEBOL" to 85),
            Triple("BOC", "Boca Juniors", "Argentina") to ("CONMEBOL" to 84),
            Triple("RAC_A", "Racing Club", "Argentina") to ("CONMEBOL" to 83),
            Triple("TOL_C", "Deportes Tolima", "Colombia") to ("CONMEBOL" to 78),
            Triple("COL_C", "Colo-Colo", "Chile") to ("CONMEBOL" to 79),
            Triple("PEN", "Peñarol", "Uruguay") to ("CONMEBOL" to 80),
            Triple("HIL", "Al Hilal", "Saudi Arabia") to ("AFC" to 86),
            Triple("NAS", "Al Nassr", "Saudi Arabia") to ("AFC" to 84),
            Triple("ITT", "Al Ittihad", "Saudi Arabia") to ("AFC" to 83),
            Triple("AHLI_S", "Al Ahli SFC", "Saudi Arabia") to ("AFC" to 83),
            Triple("AIN", "Al Ain", "UAE") to ("AFC" to 80),
            Triple("URA", "Urawa Red Diamonds", "Japan") to ("AFC" to 79),
            Triple("YFM", "Yokohama F. Marinos", "Japan") to ("AFC" to 79),
            Triple("ULS", "Ulsan HD", "South Korea") to ("AFC" to 79),
            Triple("AME", "Club América", "Mexico") to ("CONCACAF" to 82),
            Triple("MTY", "Monterrey", "Mexico") to ("CONCACAF" to 81),
            Triple("TIG", "Tigres UANL", "Mexico") to ("CONCACAF" to 81),
            Triple("MIA", "Inter Miami CF", "United States") to ("CONCACAF" to 81),
            Triple("LAFC", "LAFC", "United States") to ("CONCACAF" to 79),
            Triple("CLB", "Columbus Crew", "United States") to ("CONCACAF" to 80),
            Triple("SLB", "Benfica", "Portugal") to ("UEFA" to 86),
            Triple("SCP", "Sporting CP", "Portugal") to ("UEFA" to 86),
            Triple("FCP", "FC Porto", "Portugal") to ("UEFA" to 85),
            Triple("PSV", "PSV Eindhoven", "Netherlands") to ("UEFA" to 84),
            Triple("FEY", "Feyenoord", "Netherlands") to ("UEFA" to 82),
            Triple("AJA", "Ajax", "Netherlands") to ("UEFA" to 82),
            Triple("GAL", "Galatasaray", "Turkey") to ("UEFA" to 83),
            Triple("FEN", "Fenerbahçe", "Turkey") to ("UEFA" to 83),
            Triple("CEL_S", "Celtic", "Scotland") to ("UEFA" to 81),
            Triple("RAN", "Rangers", "Scotland") to ("UEFA" to 79),
            Triple("STP_G", "FC St. Pauli", "Germany") to ("UEFA" to 77),
            Triple("AKL", "Auckland City FC", "New Zealand") to ("OFC" to 70)
        )
        globalClubs.forEach { (info, meta) ->
            addClub(info.first, info.second, info.third, meta.first, 1, meta.second)
        }

        return list
    }

    val historicalWorldCups: List<HistoricalWorldCup> = listOf(
        // MEN'S WORLD CUP (1930 - 2026)
        HistoricalWorldCup(1930, "Men's WC", "Uruguay", "Uruguay", "Argentina", "United States", "4 - 2", 13, 70, 18, "Guillermo Stábile (8)", listOf("Group 1: ARG, CHL, FRA, MEX", "Group 2: YUG, BRA, BOL", "Group 3: URU, ROU, PER", "Group 4: USA, PAR, BEL")),
        HistoricalWorldCup(1934, "Men's WC", "Italy", "Italy", "Czechoslovakia", "Germany", "2 - 1 (a.e.t.)", 16, 70, 17, "Oldřich Nejedlý (5)", listOf("16-Team Straight Knockout Format", "Final in Rome: Italy 2-1 Czechoslovakia")),
        HistoricalWorldCup(1938, "Men's WC", "France", "Italy", "Hungary", "Brazil", "4 - 2", 15, 84, 18, "Leônidas (7)", listOf("15-Team Straight Knockout Format", "Final in Paris: Italy 4-2 Hungary")),
        HistoricalWorldCup(1950, "Men's WC", "Brazil", "Uruguay", "Brazil", "Sweden", "2 - 1 (Final Round)", 13, 88, 22, "Ademir (8)", listOf("Final Group Stage: URU, BRA, SWE, ESP", "Decisive Match (Maracanazo): Uruguay 2-1 Brazil")),
        HistoricalWorldCup(1954, "Men's WC", "Switzerland", "West Germany", "Hungary", "Austria", "3 - 2", 16, 140, 26, "Sándor Kocsis (11)", listOf("Miracle of Bern: West Germany 3-2 Hungary")),
        HistoricalWorldCup(1958, "Men's WC", "Sweden", "Brazil", "Sweden", "France", "5 - 2", 16, 126, 35, "Just Fontaine (13)", listOf("Pelé's breakout tournament; Brazil 5-2 Sweden")),
        HistoricalWorldCup(1962, "Men's WC", "Chile", "Brazil", "Czechoslovakia", "Chile", "3 - 1", 16, 89, 32, "Garrincha & Vavá (4)", listOf("Final in Santiago: Brazil 3-1 Czechoslovakia")),
        HistoricalWorldCup(1966, "Men's WC", "England", "England", "West Germany", "Portugal", "4 - 2 (a.e.t.)", 16, 89, 32, "Eusébio (9)", listOf("Final at Wembley: England 4-2 West Germany")),
        HistoricalWorldCup(1970, "Men's WC", "Mexico", "Brazil", "Italy", "West Germany", "4 - 1", 16, 95, 32, "Gerd Müller (10)", listOf("Estadio Azteca Final: Brazil 4-1 Italy")),
        HistoricalWorldCup(1974, "Men's WC", "West Germany", "West Germany", "Netherlands", "Poland", "2 - 1", 16, 97, 38, "Grzegorz Lato (7)", listOf("Final in Munich: West Germany 2-1 Netherlands")),
        HistoricalWorldCup(1978, "Men's WC", "Argentina", "Argentina", "Netherlands", "Brazil", "3 - 1 (a.e.t.)", 16, 102, 38, "Mario Kempes (6)", listOf("Final in Buenos Aires: Argentina 3-1 Netherlands")),
        HistoricalWorldCup(1982, "Men's WC", "Spain", "Italy", "West Germany", "Poland", "3 - 1", 24, 146, 52, "Paolo Rossi (6)", listOf("Notable: Algeria 2-1 West Germany in Group 2", "Final in Madrid: Italy 3-1 West Germany")),
        HistoricalWorldCup(1986, "Men's WC", "Mexico", "Argentina", "West Germany", "France", "3 - 2", 24, 132, 52, "Gary Lineker (6)", listOf("Maradona's tournament; Final: Argentina 3-2 West Germany")),
        HistoricalWorldCup(1990, "Men's WC", "Italy", "West Germany", "Argentina", "Italy", "1 - 0", 24, 115, 52, "Salvatore Schillaci (6)", listOf("Final in Rome: West Germany 1-0 Argentina")),
        HistoricalWorldCup(1994, "Men's WC", "United States", "Brazil", "Italy", "Sweden", "0 - 0 (3-2 pen)", 24, 141, 52, "Stoichkov & Salenko (6)", listOf("Rose Bowl Final: Brazil 0-0 (3-2 pen) Italy")),
        HistoricalWorldCup(1998, "Men's WC", "France", "France", "Brazil", "Croatia", "3 - 0", 32, 171, 64, "Davor Šuker (6)", listOf("First 32-team edition; Final: France 3-0 Brazil")),
        HistoricalWorldCup(2002, "Men's WC", "South Korea & Japan", "Brazil", "Germany", "Turkey", "2 - 0", 32, 161, 64, "Ronaldo (8)", listOf("Final in Yokohama: Brazil 2-0 Germany")),
        HistoricalWorldCup(2006, "Men's WC", "Germany", "Italy", "France", "Germany", "1 - 1 (5-3 pen)", 32, 147, 64, "Miroslav Klose (5)", listOf("Final in Berlin: Italy 1-1 (5-3 pen) France")),
        HistoricalWorldCup(2010, "Men's WC", "South Africa", "Spain", "Netherlands", "Germany", "1 - 0 (a.e.t.)", 32, 145, 64, "Müller, Villa, Sneijder, Forlán (5)", listOf("First African WC; Iniesta 116' winner vs Netherlands")),
        HistoricalWorldCup(2014, "Men's WC", "Brazil", "Germany", "Argentina", "Netherlands", "1 - 0 (a.e.t.)", 32, 171, 64, "James Rodríguez (6)", listOf("Götze 113' winner in Maracanã; Algeria reached R16 vs Germany (1-2 a.e.t.)")),
        HistoricalWorldCup(2018, "Men's WC", "Russia", "France", "Croatia", "Belgium", "4 - 2", 32, 169, 64, "Harry Kane (6)", listOf("Final in Moscow: France 4-2 Croatia")),
        HistoricalWorldCup(2022, "Men's WC", "Qatar", "Argentina", "France", "Croatia", "3 - 3 (4-2 pen)", 32, 172, 64, "Kylian Mbappé (8)", listOf("Lusail Final: Argentina 3-3 (4-2 pen) France; Morocco 4th Place")),
        HistoricalWorldCup(2026, "Men's WC", "USA, Canada & Mexico", "Expanded 48-Team Edition", "TBD / Simulate", "TBD", "48 Teams (12 Groups of 4)", 48, 0, 104, "Ready for Simulation", listOf("12 Groups of 4 (Top 2 + 8 Best 3rd advance to Round of 32)")),
        // WOMEN'S WORLD CUP (1991 - 2027)
        HistoricalWorldCup(1991, "Women's WC", "China PR", "United States", "Norway", "Sweden", "2 - 1", 12, 99, 26, "Michelle Akers (10)", listOf("Inaugural FIFA Women's World Cup")),
        HistoricalWorldCup(1995, "Women's WC", "Sweden", "Norway", "Germany", "United States", "2 - 0", 12, 99, 26, "Ann Kristin Aarønes (6)", listOf("Final in Stockholm: Norway 2-0 Germany")),
        HistoricalWorldCup(1999, "Women's WC", "United States", "United States", "China PR", "Brazil", "0 - 0 (5-4 pen)", 16, 123, 32, "Sissi & Sun Wen (7)", listOf("Rose Bowl Final: USA 0-0 (5-4 pen) China")),
        HistoricalWorldCup(2003, "Women's WC", "United States", "Germany", "Sweden", "United States", "2 - 1 (a.e.t.)", 16, 107, 32, "Birgit Prinz (7)", listOf("Golden goal final by Nia Künzer")),
        HistoricalWorldCup(2007, "Women's WC", "China PR", "Germany", "Brazil", "United States", "2 - 0", 16, 111, 32, "Marta (7)", listOf("Germany won tournament without conceding a single goal")),
        HistoricalWorldCup(2011, "Women's WC", "Germany", "Japan", "United States", "Sweden", "2 - 2 (3-1 pen)", 16, 86, 32, "Homare Sawa (5)", listOf("Final in Frankfurt: Japan defeated USA on penalties")),
        HistoricalWorldCup(2015, "Women's WC", "Canada", "United States", "Japan", "England", "5 - 2", 24, 146, 52, "Célia Šašić & Carli Lloyd (6)", listOf("Carli Lloyd hat-trick in Vancouver Final")),
        HistoricalWorldCup(2019, "Women's WC", "France", "United States", "Netherlands", "Sweden", "2 - 0", 24, 146, 52, "Megan Rapinoe (6)", listOf("Final in Lyon: USA 2-0 Netherlands")),
        HistoricalWorldCup(2023, "Women's WC", "Australia & New Zealand", "Spain", "England", "Sweden", "1 - 0", 32, 164, 64, "Hinata Miyazawa (5)", listOf("First 32-team Women's WC; Olga Carmona winner in Sydney")),
        HistoricalWorldCup(2027, "Women's WC", "Brazil", "32-Team Edition", "TBD / Simulate", "TBD", "Upcoming Verified Host: Brazil", 32, 0, 64, "Ready for Simulation", listOf("Hosted in Brazil across major venues")),
        // YOUTH WORLD CUPS
        HistoricalWorldCup(2023, "U-20 WC", "Argentina", "Uruguay", "Italy", "Israel", "1 - 0", 24, 153, 52, "Cesare Casadei (7)", listOf("Men's FIFA U-20 World Cup 2023 Final in La Plata")),
        HistoricalWorldCup(2023, "U-17 WC", "Indonesia", "Germany", "France", "Mali", "2 - 2 (4-3 pen)", 24, 172, 52, "Agustín Ruberto (8)", listOf("Men's FIFA U-17 World Cup 2023 Final in Surakarta")),
        HistoricalWorldCup(2024, "U-20 WC", "Colombia (Women)", "Korea DPR", "Japan", "United States", "1 - 0", 24, 148, 52, "Choe Il-son (6)", listOf("FIFA U-20 Women's World Cup 2024 Final in Bogotá")),
        HistoricalWorldCup(2024, "U-17 WC", "Dominican Rep. (Women)", "Korea DPR", "Spain", "United States", "1 - 1 (4-3 pen)", 16, 94, 32, "Kennedy Fuller (4)", listOf("FIFA U-17 Women's World Cup 2024 Final"))
    )

    val realClubSeasons: List<ClubCompetitionSeason> = listOf(
        // UEFA CHAMPIONS LEAGUE
        ClubCompetitionSeason("UCL_2627", "UEFA Champions League", "UEFA", "2026-27", "36-Club Swiss League Phase + Knockout", "In Progress / Simulate", "TBD", "League Phase Active", "Munich / Budapest", "Active Season", listOf("Real Madrid", "Manchester City", "Bayern Munich", "Liverpool", "Inter Milan", "Barcelona", "Arsenal", "PSG"), "CURRENT VERIFIED STRUCTURE"),
        ClubCompetitionSeason("UCL_2526", "UEFA Champions League", "UEFA", "2025-26", "36-Club Swiss League Phase + Knockout", "Real Madrid / Simulate", "TBD", "36-Club Format", "Budapest (Puskás Aréna)", "Verified Participants", listOf("Real Madrid", "Manchester City", "Bayern Munich", "PSG", "Inter Milan", "Liverpool", "Barcelona", "Bayer Leverkusen")),
        ClubCompetitionSeason("UCL_2425", "UEFA Champions League", "UEFA", "2024-25", "36-Club League Phase (8 Matchdays) + KO", "Paris Saint-Germain / Simulate", "Inter Milan", "League Phase Inaugural Season", "Allianz Arena, Munich", "Serhou Guirassy / Raphinha", listOf("Liverpool (1st League Phase)", "Barcelona (2nd)", "Arsenal (3rd)", "Inter Milan (4th)", "Real Madrid", "PSG", "Bayern Munich")),
        ClubCompetitionSeason("UCL_2324", "UEFA Champions League", "UEFA", "2023-24", "32-Club Group Stage + Knockout", "Real Madrid", "Borussia Dortmund", "2 - 0 (Carvajal 74', Vinícius Jr 83')", "Wembley Stadium, London", "Harry Kane & Kylian Mbappé (8)", listOf("Real Madrid", "Borussia Dortmund", "Bayern Munich", "PSG", "Manchester City", "Arsenal", "Barcelona", "Atlético Madrid")),
        ClubCompetitionSeason("UCL_2223", "UEFA Champions League", "UEFA", "2022-23", "32-Club Group Stage + Knockout", "Manchester City", "Inter Milan", "1 - 0 (Rodri 68')", "Atatürk Olympic Stadium, Istanbul", "Erling Haaland (12)", listOf("Manchester City", "Inter Milan", "Real Madrid", "AC Milan", "Bayern Munich", "Napoli", "Chelsea", "Benfica")),
        ClubCompetitionSeason("UCL_2122", "UEFA Champions League", "UEFA", "2021-22", "32-Club Group Stage + Knockout", "Real Madrid", "Liverpool", "1 - 0 (Vinícius Jr 59')", "Stade de France, Saint-Denis", "Karim Benzema (15)", listOf("Real Madrid", "Liverpool", "Manchester City", "Villarreal", "Chelsea", "Bayern Munich", "Benfica", "Atlético Madrid")),
        ClubCompetitionSeason("UCL_2021", "UEFA Champions League", "UEFA", "2020-21", "32-Club Group Stage + Knockout", "Chelsea", "Manchester City", "1 - 0 (Havertz 42')", "Estádio do Dragão, Porto", "Erling Haaland (10)", listOf("Chelsea", "Manchester City", "Real Madrid", "PSG", "Bayern Munich", "Borussia Dortmund", "FC Porto", "Liverpool")),
        // UEFA EUROPA & CONFERENCE LEAGUE
        ClubCompetitionSeason("UEL_2324", "UEFA Europa League", "UEFA", "2023-24", "32-Club Group Stage + KO", "Atalanta", "Bayer Leverkusen", "3 - 0 (Lookman 12', 26', 75')", "Aviva Stadium, Dublin", "Pierre-Emerick Aubameyang (10)", listOf("Atalanta", "Bayer Leverkusen", "AS Roma", "Olympique Marseille", "Liverpool", "AC Milan", "Benfica")),
        ClubCompetitionSeason("UECL_2324", "UEFA Conference League", "UEFA", "2023-24", "32-Club Group Stage + KO", "Olympiacos", "Fiorentina", "1 - 0 (a.e.t. El Kaabi 116')", "Agia Sophia Stadium, Athens", "Ayoub El Kaabi (11)", listOf("Olympiacos", "Fiorentina", "Aston Villa", "Club Brugge", "Fenerbahçe", "Lille")),
        // CONMEBOL LIBERTADORES & SUDAMERICANA
        ClubCompetitionSeason("LIB_2024", "Copa Libertadores", "CONMEBOL", "2024", "32-Club Group Stage + KO", "Botafogo", "Atlético Mineiro", "3 - 1", "Estadio Monumental, Buenos Aires", "Júnior Santos (10)", listOf("Botafogo", "Atlético Mineiro", "River Plate", "Peñarol", "Flamengo", "Palmeiras", "São Paulo", "Boca Juniors")),
        ClubCompetitionSeason("LIB_2023", "Copa Libertadores", "CONMEBOL", "2023", "32-Club Group Stage + KO", "Fluminense", "Boca Juniors", "2 - 1 (a.e.t.)", "Maracanã, Rio de Janeiro", "Germán Cano (13)", listOf("Fluminense", "Boca Juniors", "Palmeiras", "Internacional", "River Plate", "Racing Club", "Deportes Tolima")),
        ClubCompetitionSeason("SUD_2024", "Copa Sudamericana", "CONMEBOL", "2024", "32-Club Group Stage + KO", "Racing Club", "Cruzeiro", "3 - 1", "Asunción, Paraguay", "Adrián Martínez (10)", listOf("Racing Club", "Cruzeiro", "Corinthians", "Lanús", "Boca Juniors", "Independiente Medellín")),
        // CAF CHAMPIONS LEAGUE & CONFEDERATION CUP
        ClubCompetitionSeason("CAFCL_2324", "CAF Champions League", "CAF", "2023-24", "16-Club Group Stage + Quarterfinals", "Al Ahly SC", "Espérance de Tunis", "1 - 0 agg (0-0, 1-0)", "Cairo International Stadium", "Sankara Karamoko (4)", listOf("Al Ahly SC", "Espérance de Tunis", "Mamelodi Sundowns", "TP Mazembe", "CR Belouizdad", "Wydad AC", "ASEC Mimosas")),
        ClubCompetitionSeason("CAFCC_2324", "CAF Confederation Cup", "CAF", "2023-24", "16-Club Group Stage + KO", "Zamalek SC", "RS Berkane", "2 - 2 agg (away goals)", "Cairo / Berkane", "Abdul Aziz Issah", listOf("Zamalek SC", "RS Berkane", "USM Alger", "Dreams FC", "Stade Malien", "Modern Future")),
        ClubCompetitionSeason("CAFCC_2223", "CAF Confederation Cup", "CAF", "2022-23", "16-Club Group Stage + KO", "USM Alger", "Young Africans", "2 - 2 agg (away goals)", "Stade du 5 Juillet, Algiers", "Fiston Mayele (7)", listOf("USM Alger", "Young Africans", "ASEC Mimosas", "Marumo Gallants", "FAR Rabat", "Pyramids FC")),
        // AFC, CONCACAF, OFC
        ClubCompetitionSeason("AFCCL_2324", "AFC Champions League", "AFC", "2023-24", "40-Club Regional Groups + KO", "Al Ain", "Yokohama F. Marinos", "6 - 3 agg (1-2, 5-1)", "Hazza bin Zayed Stadium", "Soufiane Rahimi (13)", listOf("Al Ain", "Yokohama F. Marinos", "Al Hilal", "Ulsan HD", "Al Nassr", "Al Ittihad", "Kawasaki Frontale")),
        ClubCompetitionSeason("CONC_2024", "CONCACAF Champions Cup", "CONCACAF", "2024", "27-Club Knockout Bracket", "Pachuca", "Columbus Crew", "3 - 0", "Estadio Hidalgo", "Salomón Rondón (9)", listOf("Pachuca", "Columbus Crew", "Club América", "Monterrey", "Inter Miami CF", "Tigres UANL")),
        ClubCompetitionSeason("OFCCL_2024", "OFC Champions League", "OFC", "2024", "8-Club Group Stage + KO", "Auckland City FC", "Pirae", "4 - 0", "Papeete, Tahiti", "Ryan De Vries", listOf("Auckland City FC", "Pirae", "Rewa", "Ifira Black Bird"))
    )

    fun buildInitialSeasonSystems(allTeams: List<UtsTeam>): List<CountrySeasonSystem> {
        val supportedCountries = listOf(
            Triple("England", "UEFA", "FA Cup"),
            Triple("Spain", "UEFA", "Copa del Rey"),
            Triple("Germany", "UEFA", "DFB-Pokal"),
            Triple("Italy", "UEFA", "Coppa Italia"),
            Triple("France", "UEFA", "Coupe de France"),
            Triple("Algeria", "CAF", "Algerian Cup")
        )

        return supportedCountries.map { (country, conf, cupName) ->
            val countryClubs = allTeams.filter { !it.isNational && it.country.equals(country, ignoreCase = true) }
            val divs = countryClubs.groupBy { it.divisionLevel }.toSortedMap().map { (level, clubs) ->
                val divName = when (country to level) {
                    "England" to 1 -> "Premier League"
                    "England" to 2 -> "EFL Championship"
                    "England" to 3 -> "EFL League One"
                    "Spain" to 1 -> "La Liga EA Sports"
                    "Spain" to 2 -> "Segunda División"
                    "Germany" to 1 -> "Bundesliga"
                    "Germany" to 2 -> "2. Bundesliga"
                    "Italy" to 1 -> "Serie A"
                    "Italy" to 2 -> "Serie B"
                    "France" to 1 -> "Ligue 1"
                    "France" to 2 -> "Ligue 2"
                    "Algeria" to 1 -> "Ligue 1 Mobilis"
                    "Algeria" to 2 -> "Algerian Ligue 2"
                    else -> "$country Division $level"
                }
                DivisionState(
                    divisionLevel = level,
                    divisionName = divName,
                    country = country,
                    promotedSlots = if (level == 1) 0 else 2,
                    relegatedSlots = 2,
                    teams = clubs,
                    standings = CoreMatchEngine.calculateStandings(clubs, emptyList()),
                    matches = emptyList(),
                    currentMatchday = 0,
                    totalMatchdays = max(1, (clubs.size - 1) * 2)
                )
            }
            CountrySeasonSystem(
                country = country,
                confederation = conf,
                cupName = cupName,
                divisions = divs
            )
        }
    }
}

// ============================================================================
// TOURNAMENT BUILDER & SIMULATION HELPERS (WORLD CUP, QUALIFIERS, BRACKETS)
// ============================================================================
private object TournamentBuilderEngine {

    fun generateWorldCupWithQualification(
        name: String,
        year: Int,
        hostName: String,
        targetParticipantsCount: Int,
        teamsPerGroup: Int,
        runQualifiers: Boolean,
        allNationalTeams: List<UtsTeam>,
        sport: SportType = SportType.FOOTBALL
    ): ActiveTournamentState {
        val menTeams = allNationalTeams.filter { it.isNational && it.category == "Men" }
        val qualLogs = mutableListOf<String>()
        val qualifiedTeams = mutableListOf<UtsTeam>()

        // Automatic host qualification
        val hostTeam = menTeams.find { it.name.equals(hostName, ignoreCase = true) } ?: menTeams.first()
        qualifiedTeams.add(hostTeam)
        qualLogs.add("HOST AUTO-QUALIFIED: ${hostTeam.flagOrBadge} ${hostTeam.name}")

        if (runQualifiers) {
            val slotsRemaining = max(3, targetParticipantsCount - 1)
            val confAllocation = mapOf(
                "UEFA" to max(1, (slotsRemaining * 0.34).toInt()),
                "CAF" to max(1, (slotsRemaining * 0.20).toInt()),
                "AFC" to max(1, (slotsRemaining * 0.18).toInt()),
                "CONMEBOL" to max(1, (slotsRemaining * 0.14).toInt()),
                "CONCACAF" to max(1, (slotsRemaining * 0.11).toInt()),
                "OFC" to max(1, slotsRemaining - (
                    max(1, (slotsRemaining * 0.34).toInt()) +
                    max(1, (slotsRemaining * 0.20).toInt()) +
                    max(1, (slotsRemaining * 0.18).toInt()) +
                    max(1, (slotsRemaining * 0.14).toInt()) +
                    max(1, (slotsRemaining * 0.11).toInt())
                ))
            )

            confAllocation.forEach { (conf, slots) ->
                val pool = menTeams.filter { it.confederation == conf && it.id != hostTeam.id }.shuffled()
                val simulatedStandings = pool.map { team ->
                    val formPoints = team.overall * 2 + Random.nextInt(-18, 22)
                    team to formPoints
                }.sortedByDescending { it.second }

                val winners = simulatedStandings.take(slots).map { it.first }
                qualifiedTeams.addAll(winners)
                qualLogs.add("$conf QUALIFIERS ($slots slots): " + winners.joinToString(", ") { "${it.flagOrBadge} ${it.code}" })
            }
        }

        // Fill or trim to exact targetParticipantsCount
        val remainingPool = menTeams.filter { m -> qualifiedTeams.none { it.id == m.id } }.sortedByDescending { it.overall }
        while (qualifiedTeams.size < targetParticipantsCount && remainingPool.isNotEmpty()) {
            val next = remainingPool.firstOrNull { r -> qualifiedTeams.none { it.id == r.id } } ?: break
            qualifiedTeams.add(next)
        }
        val finalParticipants = qualifiedTeams.take(targetParticipantsCount)

        // Seed into 4 Pots by Overall Rating
        val sortedByRating = finalParticipants.sortedByDescending { it.overall }
        val potSize = max(1, finalParticipants.size / 4)
        val pots = sortedByRating.chunked(potSize)

        // Create Groups
        val safeTeamsPerGroup = teamsPerGroup.coerceIn(3, 8)
        val groupCount = max(1, finalParticipants.size / safeTeamsPerGroup)
        val groupBuckets = List(groupCount) { mutableListOf<UtsTeam>() }

        sortedByRating.forEachIndexed { index, team ->
            groupBuckets[index % groupCount].add(team)
        }

        val groups = groupBuckets.mapIndexed { idx, gTeams ->
            val gLetter = if (idx < 26) ('A' + idx).toString() else "G${idx + 1}"
            TournamentGroupState(
                groupName = "Group $gLetter",
                teams = gTeams,
                matches = emptyList(),
                standings = CoreMatchEngine.calculateStandings(gTeams, emptyList())
            )
        }

        return ActiveTournamentState(
            id = "WC_${System.currentTimeMillis()}",
            name = name,
            type = "WORLD_CUP",
            sport = sport,
            hostName = hostName,
            year = year,
            participants = finalParticipants,
            qualificationLogs = qualLogs,
            drawPots = pots,
            groups = groups,
            knockoutRounds = emptyList()
        )
    }

    fun simulateAllGroupsAndAdvanceToKnockout(tournament: ActiveTournamentState): ActiveTournamentState {
        val updatedGroups = tournament.groups.map { grp ->
            val matches = mutableListOf<SimulatedMatchResult>()
            for (i in grp.teams.indices) {
                for (j in i + 1 until grp.teams.size) {
                    matches.add(
                        CoreMatchEngine.simulateMatch(
                            stageName = grp.groupName,
                            home = grp.teams[i],
                            away = grp.teams[j],
                            mustHaveWinner = false,
                            neutralVenue = true,
                            sport = tournament.sport
                        )
                    )
                }
            }
            val standings = CoreMatchEngine.calculateStandings(grp.teams, matches)
            grp.copy(matches = matches, standings = standings)
        }

        // Collect top 2 from each group + best 3rd place teams to reach a power-of-2 bracket
        val directQualifiers = mutableListOf<UtsTeam>()
        val thirdPlaced = mutableListOf<StandingRow>()

        updatedGroups.forEach { g ->
            directQualifiers.addAll(g.standings.take(2).map { it.team })
            if (g.standings.size >= 3) {
                thirdPlaced.add(g.standings[2])
            }
        }

        val totalTeams = tournament.participants.size
        val targetKoSize = when {
            totalTeams >= 64 -> 32
            totalTeams >= 32 -> 16
            totalTeams >= 16 -> 8
            totalTeams >= 8 -> 4
            else -> 2
        }

        val sortedThirds = thirdPlaced.sortedWith(
            compareByDescending<StandingRow> { it.points }
                .thenByDescending { it.goalDiff }
                .thenByDescending { it.goalsFor }
        ).map { it.team }

        val koParticipants = (directQualifiers + sortedThirds).distinctBy { it.id }.take(targetKoSize).toMutableList()
        while (koParticipants.size < 2 && tournament.participants.size >= 2) {
            koParticipants.addAll(tournament.participants.take(2))
        }

        return simulateKnockoutBracketFromList(
            tournament.copy(groups = updatedGroups),
            koParticipants
        )
    }

    fun simulateKnockoutBracketFromList(
        tournament: ActiveTournamentState,
        seededTeams: List<UtsTeam>
    ): ActiveTournamentState {
        var currentTeams = seededTeams
        val rounds = mutableListOf<KnockoutRoundState>()
        var semiFinalLosers = mutableListOf<UtsTeam>()
        var runnerUpTeam: UtsTeam? = null
        var thirdPlaceTeam: UtsTeam? = null

        while (currentTeams.size >= 2) {
            val roundName = when (currentTeams.size) {
                64 -> "Round of 64"
                32 -> "Round of 32"
                16 -> "Round of 16"
                8 -> "Quarter-Finals"
                4 -> "Semi-Finals"
                2 -> "Final"
                else -> "Knockout Stage (${currentTeams.size} Teams)"
            }
            val roundMatches = mutableListOf<SimulatedMatchResult>()
            val nextRoundWinners = mutableListOf<UtsTeam>()
            val thisRoundLosers = mutableListOf<UtsTeam>()

            val pairCount = currentTeams.size / 2
            for (i in 0 until pairCount) {
                val home = currentTeams[i]
                val away = currentTeams[currentTeams.size - 1 - i]
                val match = CoreMatchEngine.simulateMatch(
                    stageName = roundName,
                    home = home,
                    away = away,
                    mustHaveWinner = true,
                    neutralVenue = true,
                    sport = tournament.sport
                )
                roundMatches.add(match)
                val w = match.winner
                val l = if (w.id == home.id) away else home
                nextRoundWinners.add(w)
                thisRoundLosers.add(l)
            }

            if (currentTeams.size == 4) {
                semiFinalLosers = thisRoundLosers
            }
            if (currentTeams.size == 2) {
                runnerUpTeam = thisRoundLosers.firstOrNull()
            }

            rounds.add(KnockoutRoundState(roundName = roundName, matches = roundMatches))
            currentTeams = nextRoundWinners
        }

        if (semiFinalLosers.size == 2) {
            val thirdMatch = CoreMatchEngine.simulateMatch(
                stageName = "Third-Place Playoff",
                home = semiFinalLosers[0],
                away = semiFinalLosers[1],
                mustHaveWinner = true,
                neutralVenue = true,
                sport = tournament.sport
            )
            thirdPlaceTeam = thirdMatch.winner
            rounds.add(KnockoutRoundState("Third-Place Playoff", listOf(thirdMatch)))
        }

        val champ = currentTeams.firstOrNull()
        return tournament.copy(
            knockoutRounds = rounds,
            champion = champ,
            runnerUp = runnerUpTeam,
            thirdPlace = thirdPlaceTeam,
            isCompleted = true
        )
    }
}
// ============================================================================
// MAIN JETPACK COMPOSE APPLICATION SHELL & ALL 5 NAVIGATION SECTIONS
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UniversalTournamentSimulatorApp() {
    val context = LocalContext.current

    var allTeams by remember {
        mutableStateOf(LocalStorageManager.loadTeams(context) ?: MasterSeedData.buildDefaultTeams())
    }
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var activeTournament by remember { mutableStateOf<ActiveTournamentState?>(null) }
    var savedTournaments by remember { mutableStateOf(listOf<ActiveTournamentState>()) }
    var selectedMatchDetail by remember { mutableStateOf<SimulatedMatchResult?>(null) }
    var seasonSystems by remember { mutableStateOf(MasterSeedData.buildInitialSeasonSystems(allTeams)) }
    var seasonCounter by remember { mutableStateOf("2026-27") }
    var dynamicRatingsEnabled by remember { mutableStateOf(true) }

    fun updateTeamsAndPersist(updated: List<UtsTeam>) {
        allTeams = updated
        LocalStorageManager.saveTeams(context, updated)
    }

    fun launchAndSaveTournament(t: ActiveTournamentState) {
        activeTournament = t
        savedTournaments = listOf(t) + savedTournaments.filter { it.id != t.id }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgSecondary)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "UNIVERSAL TOURNAMENT SIMULATOR",
                        color = AccentGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${allTeams.count { it.isNational }} Nations • ${allTeams.count { !it.isNational }} Clubs • 12 Sports",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                if (activeTournament != null) {
                    TextButton(onClick = { activeTournament = null }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = AccentGold)
                        Spacer(Modifier.width(4.dp))
                        Text("Close Hub", color = AccentGold, fontSize = 12.sp)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = BgSecondary) {
                MainTab.values().forEach { tab ->
                    val selected = currentTab == tab && activeTournament == null
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            activeTournament = null
                            currentTab = tab
                        },
                        icon = {
                            val iconVec = when (tab) {
                                MainTab.HOME -> Icons.Default.Home
                                MainTab.INTERNATIONAL -> Icons.Default.Star
                                MainTab.CLUBS -> Icons.Default.Favorite
                                MainTab.SEASON -> Icons.Default.DateRange
                                MainTab.MORE -> Icons.Default.Settings
                            }
                            Icon(iconVec, contentDescription = tab.title)
                        },
                        label = { Text(tab.title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentGreen,
                            selectedTextColor = AccentGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = CardBg
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeTournament != null) {
                ActiveTournamentHubScreen(
                    tournament = activeTournament!!,
                    onUpdateTournament = { updated ->
                        activeTournament = updated
                        savedTournaments = listOf(updated) + savedTournaments.filter { it.id != updated.id }
                    },
                    onInspectMatch = { selectedMatchDetail = it },
                    onBack = { activeTournament = null }
                )
            } else {
                when (currentTab) {
                    MainTab.HOME -> HomeDashboardScreen(
                        allTeams = allTeams,
                        savedTournaments = savedTournaments,
                        onOpenTournament = { activeTournament = it },
                        onCreateAndOpenTournament = { launchAndSaveTournament(it) },
                        onInspectMatch = { selectedMatchDetail = it },
                        onNavigateTab = { currentTab = it }
                    )
                    MainTab.INTERNATIONAL -> InternationalSectionScreen(
                        allTeams = allTeams,
                        onLaunchTournament = { launchAndSaveTournament(it) }
                    )
                    MainTab.CLUBS -> ClubsSectionScreen(
                        allTeams = allTeams,
                        onLaunchTournament = { launchAndSaveTournament(it) }
                    )
                    MainTab.SEASON -> SeasonModeScreen(
                        seasonYear = seasonCounter,
                        systems = seasonSystems,
                        dynamicRatingsEnabled = dynamicRatingsEnabled,
                        onToggleDynamicRatings = { dynamicRatingsEnabled = it },
                        onUpdateSystems = { updatedSystems, updatedYear, updatedClubs ->
                            seasonSystems = updatedSystems
                            seasonCounter = updatedYear
                            if (updatedClubs.isNotEmpty()) {
                                val mapById = updatedClubs.associateBy { it.id }
                                updateTeamsAndPersist(allTeams.map { mapById[it.id] ?: it })
                            }
                        },
                        onInspectMatch = { selectedMatchDetail = it }
                    )
                    MainTab.MORE -> MoreSectionScreen(
                        allTeams = allTeams,
                        savedTournaments = savedTournaments,
                        onUpdateTeams = { updateTeamsAndPersist(it) },
                        onLaunchTournament = { launchAndSaveTournament(it) }
                    )
                }
            }

            if (selectedMatchDetail != null) {
                MatchCenterDialog(
                    match = selectedMatchDetail!!,
                    onDismiss = { selectedMatchDetail = null }
                )
            }
        }
    }
}

// ============================================================================
// 1. HOME DASHBOARD SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeDashboardScreen(
    allTeams: List<UtsTeam>,
    savedTournaments: List<ActiveTournamentState>,
    onOpenTournament: (ActiveTournamentState) -> Unit,
    onCreateAndOpenTournament: (ActiveTournamentState) -> Unit,
    onInspectMatch: (SimulatedMatchResult) -> Unit,
    onNavigateTab: (MainTab) -> Unit
) {
    var homeIdx by remember { mutableStateOf(0) }
    var awayIdx by remember { mutableStateOf(1) }
    var latestQuickMatch by remember { mutableStateOf<SimulatedMatchResult?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF1A3A32), CardBg)))
                    .border(1.dp, AccentGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("COMMAND DASHBOARD", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Simulate World Cups, Continental Club Cups, Multi-Division Pyramids & 12 Sports",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val wc = TournamentBuilderEngine.generateWorldCupWithQualification(
                                    name = "2026 FIFA World Cup (48 Nations)",
                                    year = 2026,
                                    hostName = "United States",
                                    targetParticipantsCount = 48,
                                    teamsPerGroup = 4,
                                    runQualifiers = true,
                                    allNationalTeams = allTeams
                                )
                                onCreateAndOpenTournament(wc)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Text("Sim 2026 World Cup", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = { onNavigateTab(MainTab.SEASON) }) {
                            Text("Season Pyramid", color = TextWhite, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚡ QUICK MATCH CENTER", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        TextButton(onClick = {
                            homeIdx = Random.nextInt(allTeams.size)
                            awayIdx = (homeIdx + Random.nextInt(1, allTeams.size)) % allTeams.size
                        }) {
                            Text("Randomize Teams", color = AccentBlue, fontSize = 12.sp)
                        }
                    }

                    val tHome = allTeams.getOrElse(homeIdx) { allTeams.first() }
                    val tAway = allTeams.getOrElse(awayIdx) { allTeams.last() }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { homeIdx = (homeIdx + 1) % allTeams.size },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(tHome.flagOrBadge, fontSize = 26.sp)
                            Text(tHome.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("OVR ${tHome.overall} • ATT ${tHome.attack}", color = AccentGreen, fontSize = 11.sp)
                        }
                        Text("VS", color = TextSecondary, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 8.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { awayIdx = (awayIdx + 1) % allTeams.size },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(tAway.flagOrBadge, fontSize = 26.sp)
                            Text(tAway.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("OVR ${tAway.overall} • DEF ${tAway.defense}", color = AccentBlue, fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            latestQuickMatch = CoreMatchEngine.simulateMatch(
                                stageName = "Exhibition Match",
                                home = tHome,
                                away = tAway,
                                mustHaveWinner = true
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TextWhite)
                        Spacer(Modifier.width(6.dp))
                        Text("Simulate Match & Commentary", color = TextWhite, fontWeight = FontWeight.Bold)
                    }

                    latestQuickMatch?.let { m ->
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(BgSecondary)
                                .clickable { onInspectMatch(m) }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${m.homeTeam.name}  ${m.homeScore} - ${m.awayScore}  ${m.awayTeam.name}" +
                                    (if (m.homePenalties != null) " (${m.homePenalties}-${m.awayPenalties} pen)" else ""),
                                color = AccentGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text("Open Match Center →", color = AccentGold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Text("SPECIAL GAME MODES", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            val modes = listOf(
                Triple("World Super League", "20 Global Elite Clubs", "SUPER_LEAGUE"),
                Triple("Battle Royale 64", "64 Countries Elimination", "BATTLE_ROYALE"),
                Triple("What-If 1982 WC", "Replay Historic 1982 Edition", "WHAT_IF"),
                Triple("CAF vs UEFA Cup", "Cross-Continental Clash", "CROSS_CUP")
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(modes) { (title, sub, code) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(190.dp)
                            .clickable {
                                val pool = when (code) {
                                    "SUPER_LEAGUE" -> allTeams.filter { !it.isNational }.sortedByDescending { it.overall }.take(16)
                                    "BATTLE_ROYALE" -> allTeams.filter { it.isNational && it.category == "Men" }.shuffled().take(64)
                                    "WHAT_IF" -> allTeams.filter { it.isNational && it.category == "Men" }.sortedByDescending { it.overall }.take(24)
                                    else -> (allTeams.filter { it.confederation == "CAF" && !it.isNational }.take(8) +
                                        allTeams.filter { it.confederation == "UEFA" && !it.isNational }.take(8))
                                }
                                val t = TournamentBuilderEngine.generateWorldCupWithQualification(
                                    name = title,
                                    year = 2026,
                                    hostName = "Global Neutral",
                                    targetParticipantsCount = pool.size,
                                    teamsPerGroup = 4,
                                    runQualifiers = false,
                                    allNationalTeams = pool.map { it.copy(isNational = true, category = "Men") }
                                )
                                onCreateAndOpenTournament(t)
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(title, color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(sub, color = TextSecondary, fontSize = 11.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Tap to Launch →", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                "SAVED & RECENT TOURNAMENTS (${savedTournaments.size})",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        if (savedTournaments.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No saved tournaments yet. Launch a World Cup, Club competition or Custom League above to save progress automatically.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(savedTournaments) { tr ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTournament(tr) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tr.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "${tr.sport.displayName} • ${tr.participants.size} Teams • Champion: ${tr.champion?.name ?: "In Progress"}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Text("Continue →", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. INTERNATIONAL SECTION
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InternationalSectionScreen(
    allTeams: List<UtsTeam>,
    onLaunchTournament: (ActiveTournamentState) -> Unit
) {
    var subTab by remember { mutableStateOf(0) }
    var selectedWcFilter by remember { mutableStateOf("Men's WC") }
    var selectedConfFilter by remember { mutableStateOf("ALL") }

    var customWcName by remember { mutableStateOf("2030 FIFA Centennial World Cup") }
    var customWcHost by remember { mutableStateOf("Morocco, Spain & Portugal") }
    var customWcYear by remember { mutableStateOf(2030) }
    var customWcSize by remember { mutableStateOf(48) }
    var customWcTeamsPerGroup by remember { mutableStateOf(4) }
    var customWcRunQualifiers by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = BgDark,
        floatingActionButton = {
            if (subTab != 2) {
                FloatingActionButton(
                    onClick = { subTab = 2 },
                    containerColor = AccentGreen
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create World Cup", tint = Color.Black)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("World Cups (1930-2027)", "All FIFA Nations", "+ Custom WC").forEachIndexed { idx, label ->
                    FilterChip(
                        selected = subTab == idx,
                        onClick = { subTab = idx },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            when (subTab) {
                0 -> {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Men's WC", "Women's WC", "U-20 WC", "U-17 WC").forEach { cat ->
                            FilterChip(
                                selected = selectedWcFilter == cat,
                                onClick = { selectedWcFilter = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    val filteredWcs = MasterSeedData.historicalWorldCups.filter { it.category == selectedWcFilter }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredWcs) { wc ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${wc.year} FIFA ${wc.category}", color = AccentGold, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                        Text("Host: ${wc.host}", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("🏆 Champion: ${wc.champion}  •  🥈 Runner-Up: ${wc.runnerUp}", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("🥉 Third Place: ${wc.thirdPlace}  •  Final: ${wc.finalScore}", color = TextSecondary, fontSize = 12.sp)
                                    Text("📊 ${wc.teamsCount} Teams • ${wc.matchesPlayed} Matches • ${wc.totalGoals} Goals • Top Scorer: ${wc.topScorer}", color = TextSecondary, fontSize = 11.sp)
                                    wc.summaryGroups.forEach { gLine ->
                                        Text("• $gLine", color = AccentGreen, fontSize = 11.sp)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val simEdition = TournamentBuilderEngine.generateWorldCupWithQualification(
                                                name = "${wc.year} ${wc.category} (${wc.host}) - Replay",
                                                year = wc.year,
                                                hostName = wc.host,
                                                targetParticipantsCount = wc.teamsCount.coerceAtLeast(12),
                                                teamsPerGroup = 4,
                                                runQualifiers = false,
                                                allNationalTeams = allTeams
                                            )
                                            onLaunchTournament(simEdition)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                                    ) {
                                        Text("Replay / What-If Simulate ${wc.year} Edition", color = TextWhite, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    val confs = listOf("ALL", "UEFA", "CAF", "CONMEBOL", "AFC", "CONCACAF", "OFC", "FIFA-W")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        confs.forEach { c ->
                            FilterChip(
                                selected = selectedConfFilter == c,
                                onClick = { selectedConfFilter = c },
                                label = { Text(c, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    val nations = allTeams.filter {
                        val matchesConf = (selectedConfFilter == "ALL") or (it.confederation == selectedConfFilter)
                        it.isNational && matchesConf
                    }.sortedByDescending { it.overall }

                    Text("Showing ${nations.size} National Teams", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(nations) { idx, team ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("#${idx + 1}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(34.dp))
                                        Text(team.flagOrBadge, fontSize = 22.sp)
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(team.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("${team.confederation} • ${team.category} • ATT ${team.attack} MID ${team.midfield} DEF ${team.defense}", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (team.overall >= 85) AccentGreen else AccentBlue)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("${team.overall}", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("CUSTOM WORLD CUP & QUALIFICATION CREATOR", color = AccentGreen, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        OutlinedTextField(
                            value = customWcName,
                            onValueChange = { customWcName = it },
                            label = { Text("Tournament Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customWcHost,
                            onValueChange = { customWcHost = it },
                            label = { Text("Host Country / Countries") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Participant Count: $customWcSize Nations", color = TextWhite, fontWeight = FontWeight.Bold)
                        val sizes = listOf(4, 8, 12, 16, 24, 32, 40, 48, 64, 96, 128)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sizes.forEach { sz ->
                                FilterChip(
                                    selected = customWcSize == sz,
                                    onClick = { customWcSize = sz },
                                    label = { Text("$sz Teams") }
                                )
                            }
                        }
                        Text("Teams Per Group: $customWcTeamsPerGroup", color = TextWhite, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(3, 4, 6, 8).forEach { tpg ->
                                FilterChip(
                                    selected = customWcTeamsPerGroup == tpg,
                                    onClick = { customWcTeamsPerGroup = tpg },
                                    label = { Text("$tpg per group") }
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = customWcRunQualifiers, onCheckedChange = { customWcRunQualifiers = it })
                            Text("Simulate Full Confederation Qualifiers (UEFA, CAF, AFC, CONMEBOL, CONCACAF, OFC)", color = TextWhite, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                val created = TournamentBuilderEngine.generateWorldCupWithQualification(
                                    name = customWcName,
                                    year = customWcYear,
                                    hostName = customWcHost,
                                    targetParticipantsCount = customWcSize,
                                    teamsPerGroup = customWcTeamsPerGroup,
                                    runQualifiers = customWcRunQualifiers,
                                    allNationalTeams = allTeams
                                )
                                onLaunchTournament(created)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Text("Generate Draw, Qualifiers & Launch World Cup", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 3. CLUBS SECTION
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubsSectionScreen(
    allTeams: List<UtsTeam>,
    onLaunchTournament: (ActiveTournamentState) -> Unit
) {
    var clubTab by remember { mutableStateOf(0) }
    var customLeagueName by remember { mutableStateOf("World Super League") }
    var customLeagueSize by remember { mutableStateOf(16) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Continental Seasons", "National Leagues", "+ Custom League").forEachIndexed { idx, label ->
                FilterChip(
                    selected = clubTab == idx,
                    onClick = { clubTab = idx },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        when (clubTab) {
            0 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(MasterSeedData.realClubSeasons) { s ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${s.competitionName} (${s.seasonYear})", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(s.confederation, color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Text("Format: ${s.format}", color = TextSecondary, fontSize = 11.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("🏆 Winner: ${s.champion}  •  🥈 Runner-up: ${s.runnerUp}", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Final: ${s.finalScore}  •  Venue: ${s.venue}", color = TextSecondary, fontSize = 12.sp)
                                Text("Clubs: ${s.notableClubs.joinToString(", ")}", color = AccentBlue, fontSize = 11.sp)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val confClubs = allTeams.filter {
                                            val sameConf = (it.confederation == s.confederation) or (it.confederation == "UEFA")
                                            !it.isNational && sameConf
                                        }.sortedByDescending { it.overall }.take(16)
                                        val tr = TournamentBuilderEngine.generateWorldCupWithQualification(
                                            name = "${s.competitionName} ${s.seasonYear}",
                                            year = 2026,
                                            hostName = s.venue,
                                            targetParticipantsCount = confClubs.size,
                                            teamsPerGroup = 4,
                                            runQualifiers = false,
                                            allNationalTeams = confClubs.map { it.copy(isNational = true, category = "Men") }
                                        )
                                        onLaunchTournament(tr)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                                ) {
                                    Text("Simulate ${s.seasonYear} Season", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                val clubsByCountry = allTeams.filter { !it.isNational }.groupBy { it.country }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(clubsByCountry.keys.sorted()) { country ->
                        val clubs = clubsByCountry[country] ?: emptyList()
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("$country Football Leagues (${clubs.size} Clubs)", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(clubs.firstOrNull()?.confederation ?: "", color = AccentGreen, fontSize = 12.sp)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    clubs.sortedByDescending { it.overall }.joinToString(" • ") { "${it.name} (${it.overall})" },
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val tr = TournamentBuilderEngine.generateWorldCupWithQualification(
                                            name = "$country Premier Championship",
                                            year = 2026,
                                            hostName = country,
                                            targetParticipantsCount = clubs.size.coerceAtLeast(4),
                                            teamsPerGroup = clubs.size.coerceIn(4, 8),
                                            runQualifiers = false,
                                            allNationalTeams = clubs.map { it.copy(isNational = true, category = "Men") }
                                        )
                                        onLaunchTournament(tr)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                                ) {
                                    Text("Simulate $country League & Cup", color = TextWhite, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("CREATE CUSTOM INTERNATIONAL CLUB SUPER LEAGUE", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    OutlinedTextField(
                        value = customLeagueName,
                        onValueChange = { customLeagueName = it },
                        label = { Text("League / Cup Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Number of Elite Clubs: $customLeagueSize", color = TextWhite)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(8, 12, 16, 20, 24, 32).forEach { sz ->
                            FilterChip(
                                selected = customLeagueSize == sz,
                                onClick = { customLeagueSize = sz },
                                label = { Text("$sz Clubs") }
                            )
                        }
                    }
                    Button(
                        onClick = {
                            val selectedClubs = allTeams.filter { !it.isNational }.sortedByDescending { it.overall }.take(customLeagueSize)
                            val tr = TournamentBuilderEngine.generateWorldCupWithQualification(
                                name = customLeagueName,
                                year = 2026,
                                hostName = "International",
                                targetParticipantsCount = selectedClubs.size,
                                teamsPerGroup = selectedClubs.size.coerceIn(4, 8),
                                runQualifiers = false,
                                allNationalTeams = selectedClubs.map { it.copy(isNational = true, category = "Men") }
                            )
                            onLaunchTournament(tr)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Text("Create & Simulate Custom Super League", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. SEASON MODE
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeasonModeScreen(
    seasonYear: String,
    systems: List<CountrySeasonSystem>,
    dynamicRatingsEnabled: Boolean,
    onToggleDynamicRatings: (Boolean) -> Unit,
    onUpdateSystems: (List<CountrySeasonSystem>, String, List<UtsTeam>) -> Unit,
    onInspectMatch: (SimulatedMatchResult) -> Unit
) {
    var selectedCountryIdx by remember { mutableStateOf(0) }
    val currentSystem = systems.getOrElse(selectedCountryIdx) { systems.first() }
    var lastSeasonSummary by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SEASON MODE PYRAMID ($seasonYear)", color = AccentGreen, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dynamic OVR", color = TextSecondary, fontSize = 11.sp)
                Switch(checked = dynamicRatingsEnabled, onCheckedChange = onToggleDynamicRatings)
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            systems.forEachIndexed { idx, sys ->
                FilterChip(
                    selected = selectedCountryIdx == idx,
                    onClick = { selectedCountryIdx = idx },
                    label = { Text("${sys.country} (${sys.divisions.size} Divs)", fontSize = 11.sp) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val updatedClubsCollector = mutableListOf<UtsTeam>()
                val summaryBuilder = StringBuilder()

                val newSystems = systems.map { sys ->
                    val simulatedDivs = sys.divisions.map { div ->
                        val matches = mutableListOf<SimulatedMatchResult>()
                        val mutableTeams = div.teams.toMutableList()
                        for (i in mutableTeams.indices) {
                            for (j in mutableTeams.indices) {
                                if (i != j) {
                                    val m = CoreMatchEngine.simulateMatch(
                                        stageName = div.divisionName,
                                        home = mutableTeams[i],
                                        away = mutableTeams[j],
                                        mustHaveWinner = false
                                    )
                                    matches.add(m)
                                    if (dynamicRatingsEnabled) {
                                        mutableTeams[i] = CoreMatchEngine.applyDynamicRatingUpdate(mutableTeams[i], mutableTeams[j], m.homeScore, m.awayScore)
                                        mutableTeams[j] = CoreMatchEngine.applyDynamicRatingUpdate(mutableTeams[j], mutableTeams[i], m.awayScore, m.homeScore)
                                    }
                                }
                            }
                        }
                        val standings = CoreMatchEngine.calculateStandings(mutableTeams, matches)
                        div.copy(
                            teams = mutableTeams,
                            standings = standings,
                            matches = matches,
                            currentMatchday = div.totalMatchdays
                        )
                    }.toMutableList()

                    val allCountryClubs = simulatedDivs.flatMap { it.teams }.shuffled()
                    val cupMatches = mutableListOf<SimulatedMatchResult>()
                    var cupPool = allCountryClubs.take(16)
                    while (cupPool.size >= 2) {
                        val next = mutableListOf<UtsTeam>()
                        for (i in 0 until cupPool.size / 2) {
                            val m = CoreMatchEngine.simulateMatch(
                                stageName = "${sys.cupName} Knockout",
                                home = cupPool[i],
                                away = cupPool[cupPool.size - 1 - i],
                                mustHaveWinner = true
                            )
                            cupMatches.add(m)
                            next.add(m.winner)
                        }
                        cupPool = next
                    }
                    val cupChamp = cupPool.firstOrNull()

                    for (dIdx in 0 until simulatedDivs.size - 1) {
                        val upper = simulatedDivs[dIdx]
                        val lower = simulatedDivs[dIdx + 1]
                        val relegated = upper.standings.takeLast(2).map { it.team }
                        val promoted = lower.standings.take(2).map { it.team }

                        if (sys.country == currentSystem.country) {
                            summaryBuilder.appendLine("${sys.country}: Promoted to ${upper.divisionName}: ${promoted.joinToString { it.name }} • Relegated: ${relegated.joinToString { it.name }}")
                        }

                        val newUpperTeams = (upper.teams.filter { u -> relegated.none { it.id == u.id } } +
                            promoted.map { it.copy(divisionLevel = upper.divisionLevel) })
                        val newLowerTeams = (lower.teams.filter { l -> promoted.none { it.id == l.id } } +
                            relegated.map { it.copy(divisionLevel = lower.divisionLevel) })

                        simulatedDivs[dIdx] = upper.copy(teams = newUpperTeams)
                        simulatedDivs[dIdx + 1] = lower.copy(teams = newLowerTeams)
                    }

                    updatedClubsCollector.addAll(simulatedDivs.flatMap { it.teams })

                    val topDivStandings = simulatedDivs.firstOrNull()?.standings ?: emptyList()
                    val uclQuals = topDivStandings.take(4).map { it.team }
                    val secQuals = (topDivStandings.drop(4).take(2).map { it.team } + listOfNotNull(cupChamp)).distinctBy { it.id }

                    sys.copy(
                        divisions = simulatedDivs,
                        cupWinner = cupChamp,
                        cupMatches = cupMatches,
                        championsLeagueQualifiers = uclQuals,
                        secondaryContinentalQualifiers = secQuals
                    )
                }

                val startYr = seasonYear.take(4).toIntOrNull() ?: 2026
                val nextYr = "${startYr + 1}-${(startYr + 2).toString().takeLast(2)}"
                lastSeasonSummary = summaryBuilder.toString().ifBlank { "Season completed! Champions & Continental Qualifiers updated." }
                onUpdateSystems(newSystems, nextYr, updatedClubsCollector)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
        ) {
            Text("Simulate Full Season, Domestic Cups & Promote/Relegate →", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }

        lastSeasonSummary?.let { msg ->
            Spacer(Modifier.height(6.dp))
            Text(msg, color = AccentGold, fontSize = 11.sp)
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (currentSystem.championsLeagueQualifiers.isNotEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🏆 ${currentSystem.cupName} Winner: ${currentSystem.cupWinner?.name ?: "TBD"}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("🌍 Continental Tier 1 Qualifiers: ${currentSystem.championsLeagueQualifiers.joinToString { it.name }}", color = AccentGreen, fontSize = 12.sp)
                            Text("🌍 Continental Tier 2 Qualifiers: ${currentSystem.secondaryContinentalQualifiers.joinToString { it.name }}", color = AccentBlue, fontSize = 12.sp)
                        }
                    }
                }
            }

            items(currentSystem.divisions) { div ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(div.divisionName, color = AccentGreen, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        StandingsTableComponent(
                            standings = div.standings,
                            promotedCount = if (div.divisionLevel == 1) 4 else div.promotedSlots,
                            relegatedCount = div.relegatedSlots
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5. MORE SECTION
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreSectionScreen(
    allTeams: List<UtsTeam>,
    savedTournaments: List<ActiveTournamentState>,
    onUpdateTeams: (List<UtsTeam>) -> Unit,
    onLaunchTournament: (ActiveTournamentState) -> Unit
) {
    val context = LocalContext.current
    var moreSubTab by remember { mutableStateOf(0) }

    var searchQuery by remember { mutableStateOf("") }
    var editingTeam by remember { mutableStateOf<UtsTeam?>(null) }
    var newTeamName by remember { mutableStateOf("") }
    var newTeamCountry by remember { mutableStateOf("Algeria") }
    var newTeamConf by remember { mutableStateOf("CAF") }
    var newTeamOvr by remember { mutableStateOf(80f) }
    var newTeamAtt by remember { mutableStateOf(80f) }
    var newTeamDef by remember { mutableStateOf(80f) }
    var selectedLogoUri by remember { mutableStateOf<String?>(null) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedLogoUri = uri.toString()
        }
    }

    var selectedSport by remember { mutableStateOf(SportType.BASKETBALL) }
    var universalTourneyName by remember { mutableStateOf("Global Championship") }
    var universalSize by remember { mutableStateOf(16) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Team Editor", "12-Sport Creator", "Backup & Stats").forEachIndexed { idx, title ->
                FilterChip(
                    selected = moreSubTab == idx,
                    onClick = { moreSubTab = idx },
                    label = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        when (moreSubTab) {
            0 -> {
                Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            if (editingTeam == null) "CREATE OR EDIT TEAM / UPLOAD BADGE" else "EDITING: ${editingTeam!!.name}",
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newTeamName,
                                onValueChange = { newTeamName = it },
                                label = { Text("Team Name") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newTeamCountry,
                                onValueChange = { newTeamCountry = it },
                                label = { Text("Country") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text("Overall: ${newTeamOvr.toInt()} • Attack: ${newTeamAtt.toInt()} • Defense: ${newTeamDef.toInt()}", color = TextWhite, fontSize = 12.sp)
                        Slider(value = newTeamOvr, onValueChange = { newTeamOvr = it; newTeamAtt = it; newTeamDef = it }, valueRange = 1f..99f)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { logoPickerLauncher.launch("image/*") }) {
                                Text(if (selectedLogoUri == null) "Pick Logo from Storage" else "Logo Selected ✓", color = AccentGold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    if (newTeamName.isNotBlank()) {
                                        val ovrInt = newTeamOvr.toInt()
                                        if (editingTeam != null) {
                                            val updated = allTeams.map {
                                                if (it.id == editingTeam!!.id) it.copy(
                                                    name = newTeamName,
                                                    country = newTeamCountry,
                                                    overall = ovrInt,
                                                    attack = newTeamAtt.toInt(),
                                                    defense = newTeamDef.toInt(),
                                                    customLogoUri = selectedLogoUri ?: it.customLogoUri
                                                ) else it
                                            }
                                            onUpdateTeams(updated)
                                            editingTeam = null
                                        } else {
                                            val created = UtsTeam(
                                                id = "CUST_${System.currentTimeMillis()}",
                                                name = newTeamName,
                                                code = newTeamName.take(3).uppercase(),
                                                flagOrBadge = "⭐",
                                                confederation = newTeamConf,
                                                country = newTeamCountry,
                                                isNational = false,
                                                overall = ovrInt,
                                                attack = newTeamAtt.toInt(),
                                                midfield = ovrInt,
                                                defense = newTeamDef.toInt(),
                                                goalkeeper = ovrInt,
                                                customLogoUri = selectedLogoUri
                                            )
                                            onUpdateTeams(listOf(created) + allTeams)
                                        }
                                        newTeamName = ""
                                        selectedLogoUri = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                            ) {
                                Text(if (editingTeam == null) "Save Team" else "Update Team", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search ${allTeams.size} Teams to Edit or Delete") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))

                val filtered = allTeams.filter {
                    val qEmpty = searchQuery.isBlank()
                    val matchName = it.name.contains(searchQuery, ignoreCase = true)
                    val matchCountry = it.country.contains(searchQuery, ignoreCase = true)
                    qEmpty or matchName or matchCountry
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filtered.take(80)) { t ->
                        Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${t.flagOrBadge} ${t.name} (OVR ${t.overall})", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${t.country} • ${t.confederation} • ${if (t.customLogoUri != null) "Custom Logo Attached" else "Standard Badge"}", color = TextSecondary, fontSize = 11.sp)
                                }
                                Row {
                                    IconButton(onClick = {
                                        editingTeam = t
                                        newTeamName = t.name
                                        newTeamCountry = t.country
                                        newTeamOvr = t.overall.toFloat()
                                        newTeamAtt = t.attack.toFloat()
                                        newTeamDef = t.defense.toFloat()
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentBlue)
                                    }
                                    IconButton(onClick = {
                                        onUpdateTeams(allTeams.filter { it.id != t.id })
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("UNIVERSAL MULTI-SPORT TOURNAMENT CREATOR", color = AccentGreen, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    OutlinedTextField(
                        value = universalTourneyName,
                        onValueChange = { universalTourneyName = it },
                        label = { Text("Tournament Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Select Sport Engine:", color = TextWhite, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SportType.values().toList()) { sp ->
                            FilterChip(
                                selected = selectedSport == sp,
                                onClick = { selectedSport = sp },
                                label = { Text("${sp.icon} ${sp.displayName}") }
                            )
                        }
                    }
                    Text("Participants: $universalSize", color = TextWhite)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(8, 16, 32, 64).forEach { sz ->
                            FilterChip(
                                selected = universalSize == sz,
                                onClick = { universalSize = sz },
                                label = { Text("$sz Teams") }
                            )
                        }
                    }
                    Button(
                        onClick = {
                            val t = TournamentBuilderEngine.generateWorldCupWithQualification(
                                name = "${selectedSport.icon} $universalTourneyName",
                                year = 2026,
                                hostName = "Global Arena",
                                targetParticipantsCount = universalSize,
                                teamsPerGroup = 4,
                                runQualifiers = false,
                                allNationalTeams = allTeams,
                                sport = selectedSport
                            )
                            onLaunchTournament(t)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Text("Launch ${selectedSport.displayName} Tournament", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            2 -> {
                val jsonPreview = remember(allTeams, savedTournaments) {
                    LocalStorageManager.exportAllAsJsonString(allTeams, savedTournaments)
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("GLOBAL DATABASE STATISTICS & JSON EXPORT", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Registered Teams: ${allTeams.size}", color = TextWhite, fontWeight = FontWeight.Bold)
                            Text("Highest Rated Nation: ${allTeams.filter { it.isNational }.maxByOrNull { it.overall }?.name ?: "-"}", color = AccentGold, fontSize = 12.sp)
                            Text("Highest Rated Club: ${allTeams.filter { !it.isNational }.maxByOrNull { it.overall }?.name ?: "-"}", color = AccentBlue, fontSize = 12.sp)
                            Text("Saved Tournaments in Session: ${savedTournaments.size}", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, jsonPreview)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Tournament Data JSON"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = TextWhite)
                        Spacer(Modifier.width(6.dp))
                        Text("Share / Export Full JSON Backup", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = BgSecondary), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = jsonPreview,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// ACTIVE TOURNAMENT HUB
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveTournamentHubScreen(
    tournament: ActiveTournamentState,
    onUpdateTournament: (ActiveTournamentState) -> Unit,
    onInspectMatch: (SimulatedMatchResult) -> Unit,
    onBack: () -> Unit
) {
    var hubTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(tournament.name, color = AccentGold, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Text(
                    "Host: ${tournament.hostName} • ${tournament.participants.size} Teams • Sport: ${tournament.sport.displayName}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                if (tournament.champion != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "🏆 CHAMPION: ${tournament.champion.flagOrBadge} ${tournament.champion.name}  •  🥈 ${tournament.runnerUp?.name ?: "-"}  •  🥉 ${tournament.thirdPlace?.name ?: "-"}",
                        color = AccentGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val simulated = TournamentBuilderEngine.simulateAllGroupsAndAdvanceToKnockout(tournament)
                            onUpdateTournament(simulated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Text(
                            if (tournament.isCompleted) "Re-Simulate Entire Tournament" else "Simulate Groups & Knockout Bracket",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Groups (${tournament.groups.size})", "Knockout Bracket", "Draw & Qualifiers").forEachIndexed { idx, t ->
                FilterChip(
                    selected = hubTab == idx,
                    onClick = { hubTab = idx },
                    label = { Text(t, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        when (hubTab) {
            0 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tournament.groups) { grp ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(grp.groupName, color = AccentGreen, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                Spacer(Modifier.height(6.dp))
                                StandingsTableComponent(standings = grp.standings, promotedCount = 2, relegatedCount = 0)
                                if (grp.matches.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Group Fixtures (Tap for Match Center):", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    grp.matches.forEach { m ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onInspectMatch(m) }
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${m.homeTeam.flagOrBadge} ${m.homeTeam.name}", color = TextWhite, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                            Text("${m.homeScore} - ${m.awayScore}", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("${m.awayTeam.name} ${m.awayTeam.flagOrBadge}", color = TextWhite, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                if (tournament.knockoutRounds.isEmpty()) {
                    Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Tap 'Simulate Groups & Knockout Bracket' above to generate the full knockout stages, extra-time, penalty shootouts, and final!",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(tournament.knockoutRounds) { round ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(round.roundName.uppercase(), color = AccentGold, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    Spacer(Modifier.height(6.dp))
                                    round.matches.forEach { m ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(BgSecondary)
                                                .clickable { onInspectMatch(m) }
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${m.homeTeam.flagOrBadge} ${m.homeTeam.name}", color = TextWhite, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                            val penText = if (m.homePenalties != null) " (${m.homePenalties}-${m.awayPenalties}p)" else if (m.extraTime) " (AET)" else ""
                                            Text("${m.homeScore} - ${m.awayScore}$penText", color = AccentGreen, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                            Text("${m.awayTeam.name} ${m.awayTeam.flagOrBadge}", color = TextWhite, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                        }
                                        Spacer(Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (tournament.qualificationLogs.isNotEmpty()) {
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("CONFEDERATION QUALIFICATION SUMMARY", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(Modifier.height(6.dp))
                                    tournament.qualificationLogs.forEach { log ->
                                        Text("• $log", color = TextWhite, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                    itemsIndexed(tournament.drawPots) { idx, pot ->
                        Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("SEEDED DRAW POT ${idx + 1}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(pot.joinToString("  •  ") { "${it.flagOrBadge} ${it.name} (${it.overall})" }, color = TextWhite, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// REUSABLE STANDINGS TABLE & MATCH CENTER DIALOG
// ============================================================================
@Composable
private fun StandingsTableComponent(
    standings: List<StandingRow>,
    promotedCount: Int,
    relegatedCount: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSecondary)
                .padding(vertical = 6.dp, horizontal = 8.dp)
        ) {
            Text("#", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(22.dp))
            Text("Team", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("P", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            Text("W", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            Text("D", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            Text("L", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            Text("GD", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
            Text("PTS", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
        }
        standings.forEachIndexed { idx, row ->
            val posColor = when {
                idx < promotedCount -> AccentGreen
                relegatedCount > 0 && idx >= standings.size - relegatedCount -> AccentRed
                else -> TextWhite
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${idx + 1}", color = posColor, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(22.dp))
                Text("${row.team.flagOrBadge} ${row.team.name}", color = TextWhite, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text("${row.played}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text("${row.won}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text("${row.drawn}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text("${row.lost}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text("${if (row.goalDiff > 0) "+${row.goalDiff}" else "${row.goalDiff}"}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                Text("${row.points}", color = AccentGreen, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
private fun MatchCenterDialog(
    match: SimulatedMatchResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(match.stageName.uppercase(), color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${match.homeTeam.flagOrBadge} ${match.homeTeam.name}  ${match.homeScore} - ${match.awayScore}  ${match.awayTeam.name} ${match.awayTeam.flagOrBadge}",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                if (match.homePenalties != null) {
                    Text("Penalties: ${match.homePenalties} - ${match.awayPenalties}", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (match.sport == SportType.FOOTBALL) {
                    StatComparisonRow("Possession", "${match.homePossession}%", "${100 - match.homePossession}%")
                    StatComparisonRow("Total Shots", "${match.homeShots}", "${match.awayShots}")
                    StatComparisonRow("Shots on Target", "${match.homeShotsOnTarget}", "${match.awayShotsOnTarget}")
                    StatComparisonRow("Corners", "${match.homeCorners}", "${match.awayCorners}")
                    StatComparisonRow("Fouls", "${match.homeFouls}", "${match.awayFouls}")
                    StatComparisonRow("Yellow / Red Cards", "${match.homeYellows}Y / ${match.homeReds}R", "${match.awayYellows}Y / ${match.awayReds}R")
                }
                Divider(color = BgSecondary)
                Text("MATCH COMMENTARY & KEY EVENTS", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                if (match.events.isEmpty()) {
                    Text("0' - 90' Tactical midfield battle with no major card or goal incidents.", color = TextSecondary, fontSize = 12.sp)
                } else {
                    match.events.forEach { ev ->
                        Text(ev.description, color = TextWhite, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Match Center", color = AccentGreen, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun StatComparisonRow(label: String, homeVal: String, awayVal: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSecondary, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(homeVal, color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(awayVal, color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
