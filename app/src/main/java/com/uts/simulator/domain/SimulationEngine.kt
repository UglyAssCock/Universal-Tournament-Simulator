package com.uts.simulator.domain

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

enum class SportType(
    val displayName: String,
    val icon: String,
    val supportsDrawsInRegularTime: Boolean,
    val defaultPeriods: Int,
    val periodName: String
) {
    FOOTBALL("Football", "⚽", true, 2, "Half"),
    BASKETBALL("Basketball", "🏀", false, 4, "Quarter"),
    TENNIS("Tennis", "🎾", false, 3, "Set"),
    VOLLEYBALL("Volleyball", "🏐", false, 5, "Set"),
    HANDBALL("Handball", "🤾", true, 2, "Half"),
    RUGBY("Rugby", "🏉", true, 2, "Half"),
    CRICKET("Cricket", "🏏", false, 2, "Innings"),
    ESPORTS("Esports", "🎮", false, 3, "Map"),
    BOXING("Boxing", "🥊", true, 12, "Round"),
    MMA("MMA", "🥋", false, 5, "Round"),
    TABLE_TENNIS("Table Tennis", "🏓", false, 5, "Game"),
    BADMINTON("Badminton", "🏸", false, 3, "Game"),
    BASEBALL("Baseball", "⚾", false, 9, "Inning")
}

enum class Confederation(val code: String, val fullName: String, val region: String, val defaultWcSlots48: Int) {
    UEFA("UEFA", "Union of European Football Associations", "Europe", 16),
    CAF("CAF", "Confederation of African Football", "Africa", 9),
    AFC("AFC", "Asian Football Confederation", "Asia", 8),
    CONMEBOL("CONMEBOL", "South American Football Confederation", "South America", 6),
    CONCACAF("CONCACAF", "Confederation of North, Central America and Caribbean", "North/Central America", 6),
    OFC("OFC", "Oceania Football Confederation", "Oceania", 1)
}

enum class TeamCategory(val label: String) {
    MENS_NATIONAL("Men's National"),
    WOMENS_NATIONAL("Women's National"),
    U20_MENS_NATIONAL("Men's U-20"),
    U17_MENS_NATIONAL("Men's U-17"),
    U20_WOMENS_NATIONAL("Women's U-20"),
    U17_WOMENS_NATIONAL("Women's U-17"),
    MENS_CLUB("Men's Club"),
    MULTI_SPORT("Multi-Sport Team")
}

enum class TournamentFormat(val label: String, val description: String) {
    GROUP_PLUS_KNOCKOUT("Groups + Knockout", "Classic World Cup / Continental format with group stage and bracket"),
    SWISS_SYSTEM("Swiss League + Knockout", "Modern 36-team UEFA League Phase followed by knockout playoffs"),
    SINGLE_ELIMINATION("Single Elimination", "Direct knockout bracket from Round 1 to the Final"),
    DOUBLE_ELIMINATION("Double Elimination", "Winners and Losers brackets; two losses required for elimination"),
    ROUND_ROBIN("Single Round Robin", "Every team plays each other once in a league table"),
    DOUBLE_ROUND_ROBIN("Double Round Robin", "Standard home-and-away domestic league format"),
    TWO_LEGGED_KNOCKOUT("Two-Legged Knockout", "Home and away aggregate ties with single-match Final"),
    BEST_OF_3("Best of 3 Series", "First team to win 2 matches advances"),
    BEST_OF_5("Best of 5 Series", "First team to win 3 matches advances"),
    BEST_OF_7("Best of 7 Series", "First team to win 4 matches advances"),
    SURVIVAL_ELIMINATION("Survival Mode", "Lowest performing teams are eliminated every single round"),
    BATTLE_ROYALE("Battle Royale", "All teams clash across multi-wave elimination rounds until 1 remains")
}

enum class DataSourceType(val badge: String) {
    VERIFIED_HISTORICAL("Verified Historical"),
    VERIFIED_CURRENT("Verified 2026-27"),
    USER_CUSTOM("Custom Created"),
    SIMULATED_FUTURE("Simulated Season")
}

enum class SimSpeed(val label: String, val delayMs: Long) {
    INSTANT("Instant", 0L),
    FAST("Fast (2s)", 25L),
    REALISTIC("Realistic Pace", 80L),
    ANIMATED("Animated Live", 160L)
}

data class UtsTeam(
    val id: String,
    val name: String,
    val shortCode: String,
    val country: String,
    val flagEmoji: String,
    val customLogoUri: String? = null,
    val confederation: Confederation = Confederation.UEFA,
    val category: TeamCategory = TeamCategory.MENS_NATIONAL,
    val sport: SportType = SportType.FOOTBALL,
    val overallRating: Int = 75,
    val attackRating: Int = 75,
    val midfieldRating: Int = 75,
    val defenseRating: Int = 75,
    val goalkeeperRating: Int = 75,
    val form: Int = 0, // -10 to +10
    val homeAdvantage: Int = 3,
    val stadiumName: String = "National Arena",
    val primaryColorHex: String = "#35D07F",
    val secondaryColorHex: String = "#191F2B",
    val domesticTier: Int = 1,
    val ratingHistory: List<Int> = listOf(overallRating),
    val titlesWon: Int = 0
)

enum class MatchEventType(val icon: String) {
    GOAL("⚽"),
    PENALTY_GOAL("🎯"),
    OWN_GOAL("🥅"),
    MISSED_PENALTY("❌"),
    YELLOW_CARD("🟨"),
    RED_CARD("🟥"),
    INJURY("🚑"),
    SUBSTITUTION("🔄"),
    PERIOD_MARKER("⏱️")
}

data class MatchEventRecord(
    val minute: Int,
    val extraMinute: Int = 0,
    val type: MatchEventType,
    val teamId: String,
    val teamName: String,
    val playerName: String,
    val description: String,
    val runningScore: String
)

data class MatchStatsRecord(
    val homePossession: Int = 50,
    val awayPossession: Int = 50,
    val homeShots: Int = 0,
    val awayShots: Int = 0,
    val homeShotsOnTarget: Int = 0,
    val awayShotsOnTarget: Int = 0,
    val homeXg: Double = 0.0,
    val awayXg: Double = 0.0,
    val homeCorners: Int = 0,
    val awayCorners: Int = 0,
    val homeFouls: Int = 0,
    val awayFouls: Int = 0,
    val homeYellowCards: Int = 0,
    val awayYellowCards: Int = 0,
    val homeRedCards: Int = 0,
    val awayRedCards: Int = 0
)

data class UtsMatch(
    val id: String,
    val tournamentId: String,
    val stageName: String, // e.g., "Group A", "Round of 16", "Quarter-Finals", "Semi-Finals", "Third Place", "Final", "Matchday 1"
    val groupName: String? = null,
    val roundNumber: Int = 1,
    val legNumber: Int = 1,
    val homeTeamId: String,
    val homeTeamName: String,
    val homeFlag: String,
    val homeRating: Int,
    val awayTeamId: String,
    val awayTeamName: String,
    val awayFlag: String,
    val awayRating: Int,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homeExtraTimeScore: Int? = null,
    val awayExtraTimeScore: Int? = null,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
    val isPlayed: Boolean = false,
    val isKnockout: Boolean = false,
    val requiresWinner: Boolean = false,
    val neutralVenue: Boolean = false,
    val venueName: String = "Main Stadium",
    val sport: SportType = SportType.FOOTBALL,
    val events: List<MatchEventRecord> = emptyList(),
    val stats: MatchStatsRecord = MatchStatsRecord(),
    val periodScores: List<Pair<Int, Int>> = emptyList()
) {
    fun winnerTeamId(): String? {
        if (!isPlayed) return null
        if (homePenalties != null && awayPenalties != null && homePenalties != awayPenalties) {
            return if (homePenalties > awayPenalties) homeTeamId else awayTeamId
        }
        return when {
            homeScore > awayScore -> homeTeamId
            awayScore > homeScore -> awayTeamId
            else -> null
        }
    }

    fun formattedScore(): String {
        if (!isPlayed) return "vs"
        val base = "$homeScore -$awayScore"
        return when {
            homePenalties != null && awayPenalties != null -> "$base (${homePenalties}-${awayPenalties} pens)"
            homeExtraTimeScore != null -> "$base (AET)"
            else -> base
        }
    }
}

data class StandingsRow(
    val teamId: String,
    val teamName: String,
    val flagEmoji: String,
    val groupName: String,
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val points: Int = 0,
    val rating: Int = 75,
    val formGuide: List<String> = emptyList(), // "W", "D", "L"
    val statusBadge: String = "" // "Q", "PO", "PROMOTED", "RELEGATED", "UCL", "UEL"
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
}

data class HistoricalWorldCupEdition(
    val year: Int,
    val category: TeamCategory,
    val hostCountries: String,
    val hostFlag: String,
    val teamsCount: Int,
    val championName: String,
    val championFlag: String,
    val runnerUpName: String,
    val runnerUpFlag: String,
    val thirdPlaceName: String,
    val thirdPlaceFlag: String,
    val fourthPlaceName: String,
    val finalScoreText: String,
    val venueFinal: String,
    val topScorer: String,
    val totalGoals: Int,
    val matchesPlayed: Int,
    val dataSource: DataSourceType = DataSourceType.VERIFIED_HISTORICAL,
    val notableMatches: List<String> = emptyList()
)

data class HistoricalClubSeasonRecord(
    val competitionId: String,
    val competitionName: String,
    val confederation: Confederation,
    val country: String,
    val seasonLabel: String, // e.g., "2026-27", "2025-26", "2024-25"
    val championName: String,
    val championFlag: String,
    val runnerUpName: String,
    val finalScoreOrPoints: String,
    val topScorer: String,
    val formatDescription: String,
    val dataSource: DataSourceType,
    val keyFixtures: List<String>
)

data class TournamentInstance(
    val id: String,
    val name: String,
    val subtitle: String,
    val yearLabel: String,
    val hostCountries: List<String>,
    val sport: SportType = SportType.FOOTBALL,
    val category: TeamCategory = TeamCategory.MENS_NATIONAL,
    val format: TournamentFormat = TournamentFormat.GROUP_PLUS_KNOCKOUT,
    val dataSource: DataSourceType = DataSourceType.USER_CUSTOM,
    val teamsCount: Int = 32,
    val groupsCount: Int = 8,
    val teamsPerGroup: Int = 4,
    val advancePerGroup: Int = 2,
    val bestThirdPlacedCount: Int = 0,
    val hasThirdPlaceMatch: Boolean = true,
    val twoLeggedKnockout: Boolean = false,
    val pointsForWin: Int = 3,
    val pointsForDraw: Int = 1,
    val pointsForLoss: Int = 0,
    val participantTeamIds: List<String> = emptyList(),
    val groupsMap: Map<String, List<String>> = emptyMap(), // "Group A" -> listOf(teamIds)
    val matches: List<UtsMatch> = emptyList(),
    val drawHistoryLog: List<String> = emptyList(),
    val currentPhase: String = "Group Stage",
    val isCompleted: Boolean = false,
    val championTeamId: String? = null,
    val championName: String? = null,
    val runnerUpName: String? = null,
    val thirdPlaceName: String? = null,
    val qualificationCompleted: Boolean = true,
    val qualificationMatches: List<UtsMatch> = emptyList()
)

data class DivisionModel(
    val id: String,
    val country: String,
    val flagEmoji: String,
    val confederation: Confederation,
    val tierLevel: Int, // 1 = Top flight, 2 = 2nd division, etc.
    val name: String,
    val teamIds: List<String>,
    val autoPromotionSlots: Int = 2,
    val playoffPromotionSlots: Int = 1,
    val relegationSlots: Int = 3,
    val championsLeagueSlots: Int = 4,
    val secondaryContinentalSlots: Int = 2,
    val matches: List<UtsMatch> = emptyList()
)

data class SeasonModeState(
    val id: String = "season_career_1",
    val seasonYear: Int = 2026,
    val dynamicRatingsEnabled: Boolean = true,
    val activeCountry: String = "England",
    val divisions: List<DivisionModel> = emptyList(),
    val domesticCups: List<TournamentInstance> = emptyList(),
    val continentalCups: List<TournamentInstance> = emptyList(),
    val seasonHistoryLogs: List<String> = emptyList(),
    val currentMatchday: Int = 1,
    val totalMatchdays: Int = 38
)

object SimulationEngine {

    private val firstNames = listOf(
        "Kylian", "Erling", "Jude", "Vinicius", "Mohamed", "Lamine", "Rodri", "Lautaro", "Harry",
        "Bukayo", "Phil", "Riyad", "Islam", "Ramy", "Ismael", "Yacine", "Achraf", "Victor",
        "Kevin", "Bruno", "Pedri", "Gavi", "Jamal", "Florian", "Federico", "Julian", "Antoine",
        "Ousmane", "Rafael", "Cole", "Martin", "Declan", "William", "Ruben", "Virgil", "Alisson",
        "Thibaut", "Emiliano", "heung-Min", "Takefusa", "Mehdi", "Salem", "Akram", "Baghdad"
    )

    private val lastNames = listOf(
        "Mbappé", "Haaland", "Bellingham", "Junior", "Salah", "Yamal", "Hernandez", "Martinez", "Kane",
        "Saka", "Foden", "Mahrez", "Slimani", "Bensebaini", "Bennacer", "Brahimi", "Hakimi", "Osimhen",
        "De Bruyne", "Fernandes", "Gonzalez", "Paez", "Musiala", "Wirtz", "Valverde", "Alvarez", "Griezmann",
        "Dembélé", "Leão", "Palmer", "Ødegaard", "Rice", "Saliba", "Dias", "van Dijk", "Becker",
        "Courtois", "Dibu", "Son", "Kubo", "Taremi", "Al-Dawsari", "Afif", "Bounedjah", "Amoura"
    )

    fun generatePlayerName(seed: Int = Random.nextInt()): String {
        val rng = Random(seed)
        return "${firstNames[rng.nextInt(firstNames.size)]}${lastNames[rng.nextInt(lastNames.size)]}"
    }

    /**
     * Simulates any match across all 13 supported sports with full event generation,
     * realistic rating influence, upsets, extra time, and penalty shootouts.
     */
    fun simulateMatch(
        match: UtsMatch,
        homeTeam: UtsTeam?,
        awayTeam: UtsTeam?,
        randomnessFactor: Double = 1.0 // 0.5 = strict ratings, 1.0 = realistic, 1.6 = chaos/upsets
    ): UtsMatch {
        if (match.sport != SportType.FOOTBALL) {
            return simulateOtherSportMatch(match, homeTeam, awayTeam, randomnessFactor)
        }

        val hOvr = homeTeam?.overallRating ?: match.homeRating
        val aOvr = awayTeam?.overallRating ?: match.awayRating
        val hAtt = homeTeam?.attackRating ?: hOvr
        val hMid = homeTeam?.midfieldRating ?: hOvr
        val hDef = homeTeam?.defenseRating ?: hOvr
        val hGk = homeTeam?.goalkeeperRating ?: hOvr
        val hForm = homeTeam?.form ?: 0
        val hAdv = if (match.neutralVenue) 0 else (homeTeam?.homeAdvantage ?: 3)

        val aAtt = awayTeam?.attackRating ?: aOvr
        val aMid = awayTeam?.midfieldRating ?: aOvr
        val aDef = awayTeam?.defenseRating ?: aOvr
        val aGk = awayTeam?.goalkeeperRating ?: aOvr
        val aForm = awayTeam?.form ?: 0

        // Calculate tactical dominance & expected goals (xG)
        val homeEffective = (hAtt * 0.40 + hMid * 0.35 + hOvr * 0.25) + hAdv + (hForm * 0.4)
        val awayEffective = (aAtt * 0.40 + aMid * 0.35 + aOvr * 0.25) + (aForm * 0.4)
        val homeDefResistance = (hDef * 0.65 + hGk * 0.35)
        val awayDefResistance = (aDef * 0.65 + aGk * 0.35)

        val homeDiff = (homeEffective - awayDefResistance) / (18.0 * randomnessFactor.coerceAtLeast(0.4))
        val awayDiff = (awayEffective - homeDefResistance) / (18.0 * randomnessFactor.coerceAtLeast(0.4))

        val baseHomeLambda = (1.35 * exp(homeDiff)).coerceIn(0.18, 4.85)
        val baseAwayLambda = (1.12 * exp(awayDiff)).coerceIn(0.15, 4.45)

        val homeGoalsRegular = samplePoisson(baseHomeLambda)
        val awayGoalsRegular = samplePoisson(baseAwayLambda)

        val events = mutableListOf<MatchEventRecord>()
        var currentH = 0
        var currentA = 0
        var hYellow = 0
        var aYellow = 0
        var hRed = 0
        var aRed = 0

        // Schedule regular time goal minutes
        val goalMinutes = mutableListOf<Pair<Int, Boolean>>() // minute to isHome
        repeat(homeGoalsRegular) { goalMinutes.add(Random.nextInt(1, 91) to true) }
        repeat(awayGoalsRegular) { goalMinutes.add(Random.nextInt(1, 91) to false) }
        goalMinutes.sortBy { it.first }

        events.add(
            MatchEventRecord(
                minute = 1,
                type = MatchEventType.PERIOD_MARKER,
                teamId = match.homeTeamId,
                teamName = match.homeTeamName,
                playerName = "",
                description = "1' Kick-off at ${match.venueName}! ${match.homeTeamName} vs ${match.awayTeamName}.",
                runningScore = "0 - 0"
            )
        )

        // Generate cards, injuries, subs, and goals across 90 minutes
        for (min in 2..90) {
            if (min == 45) {
                events.add(
                    MatchEventRecord(
                        minute = 45,
                        type = MatchEventType.PERIOD_MARKER,
                        teamId = match.homeTeamId,
                        teamName = match.homeTeamName,
                        playerName = "",
                        description = "45' Half-Time whistle: ${match.homeTeamName} $currentH - $currentA ${match.awayTeamName}.",
                        runningScore = "$currentH - $currentA"
                    )
                )
            }

            goalMinutes.filter { it.first == min }.forEach { (_, isHomeGoal) ->
                if (isHomeGoal) currentH++ else currentA++
                val scoringTeamId = if (isHomeGoal) match.homeTeamId else match.awayTeamId
                val scoringTeamName = if (isHomeGoal) match.homeTeamName else match.awayTeamName
                val scorer = generatePlayerName()
                val roll = Random.nextInt(100)
                val eventType = when {
                    roll < 11 -> MatchEventType.PENALTY_GOAL
                    roll < 15 -> MatchEventType.OWN_GOAL
                    else -> MatchEventType.GOAL
                }
                val desc = when (eventType) {
                    MatchEventType.PENALTY_GOAL -> "$min' PENALTY GOAL! $scorer converts calmly from the spot for $scoringTeamName!"
                    MatchEventType.OWN_GOAL -> "$min' OWN GOAL! Deflection goes inside the net, gifting $scoringTeamName a goal!"
                    else -> "$min' GOAL! Brilliant finish by $scorer for $scoringTeamName! ($currentH-$currentA)"
                }
                events.add(
                    MatchEventRecord(
                        minute = min,
                        type = eventType,
                        teamId = scoringTeamId,
                        teamName = scoringTeamName,
                        playerName = scorer,
                        description = desc,
                        runningScore = "$currentH - $currentA"
                    )
                )
            }

            // Discipline & tactical events
            val cardRoll = Random.nextInt(1000)
            if (cardRoll < 28) {
                val isHomeCard = Random.nextBoolean()
                if (isHomeCard) hYellow++ else aYellow++
                val tId = if (isHomeCard) match.homeTeamId else match.awayTeamId
                val tName = if (isHomeCard) match.homeTeamName else match.awayTeamName
                val player = generatePlayerName()
                events.add(
                    MatchEventRecord(
                        minute = min,
                        type = MatchEventType.YELLOW_CARD,
                        teamId = tId,
                        teamName = tName,
                        playerName = player,
                        description = "$min' Yellow card shown to $player ($tName) for a tactical foul.",
                        runningScore = "$currentH - $currentA"
                    )
                )
            } else if (cardRoll == 998) {
                val isHomeRed = Random.nextBoolean()
                if (isHomeRed) hRed++ else aRed++
                val tId = if (isHomeRed) match.homeTeamId else match.awayTeamId
                val tName = if (isHomeRed) match.homeTeamName else match.awayTeamName
                val player = generatePlayerName()
                events.add(
                    MatchEventRecord(
                        minute = min,
                        type = MatchEventType.RED_CARD,
                        teamId = tId,
                        teamName = tName,
                        playerName = player,
                        description = "$min' RED CARD! $player ($tName) is sent off after a reckless challenge!",
                        runningScore = "$currentH - $currentA"
                    )
                )
            } else if (min in 60..82 && cardRoll in 30..42) {
                val isHomeSub = Random.nextBoolean()
                val tId = if (isHomeSub) match.homeTeamId else match.awayTeamId
                val tName = if (isHomeSub) match.homeTeamName else match.awayTeamName
                val pOn = generatePlayerName()
                val pOff = generatePlayerName()
                events.add(
                    MatchEventRecord(
                        minute = min,
                        type = MatchEventType.SUBSTITUTION,
                        teamId = tId,
                        teamName = tName,
                        playerName = pOn,
                        description = "$min' Substitution for $tName: $pOn comes on for $pOff.",
                        runningScore = "$currentH - $currentA"
                    )
                )
            }
        }

        var etHome: Int? = null
        var etAway: Int? = null
        var pensHome: Int? = null
        var pensAway: Int? = null

        // Handle Extra Time and Penalty Shootout if knockout requires a winner
        if ((match.isKnockout || match.requiresWinner) && currentH == currentA) {
            events.add(
                MatchEventRecord(
                    minute = 90,
                    type = MatchEventType.PERIOD_MARKER,
                    teamId = match.homeTeamId,
                    teamName = match.homeTeamName,
                    playerName = "",
                    description = "90' End of Regular Time ($currentH-$currentA). Heading into 30 minutes of Extra Time!",
                    runningScore = "$currentH - $currentA"
                )
            )
            val etGoalsH = samplePoisson(baseHomeLambda * 0.26)
            val etGoalsA = samplePoisson(baseAwayLambda * 0.26)

            repeat(etGoalsH) {
                currentH++
                val m = Random.nextInt(91, 121)
                val p = generatePlayerName()
                events.add(
                    MatchEventRecord(
                        minute = m,
                        type = MatchEventType.GOAL,
                        teamId = match.homeTeamId,
                        teamName = match.homeTeamName,
                        playerName = p,
                        description = "$m' EXTRA TIME GOAL! $p scores a dramatic strike for ${match.homeTeamName}!",
                        runningScore = "$currentH - $currentA"
                    )
                )
            }
            repeat(etGoalsA) {
                currentA++
                val m = Random.nextInt(91, 121)
                val p = generatePlayerName()
                events.add(
                    MatchEventRecord(
                        minute = m,
                        type = MatchEventType.GOAL,
                        teamId = match.awayTeamId,
                        teamName = match.awayTeamName,
                        playerName = p,
                        description = "$m' EXTRA TIME GOAL! $p silences the crowd for ${match.awayTeamName}!",
                        runningScore = "$currentH - $currentA"
                    )
                )
            }
            etHome = currentH
            etAway = currentA

            if (currentH == currentA) {
                val (pkH, pkA, pkDesc) = simulatePenaltyShootout(match.homeTeamName, match.awayTeamName, hGk, aGk)
                pensHome = pkH
                pensAway = pkA
                events.add(
                    MatchEventRecord(
                        minute = 120,
                        type = MatchEventType.PENALTY_GOAL,
                        teamId = if (pkH > pkA) match.homeTeamId else match.awayTeamId,
                        teamName = if (pkH > pkA) match.homeTeamName else match.awayTeamName,
                        playerName = "Penalty Shootout",
                        description = pkDesc,
                        runningScore = "$currentH - $currentA ($pkH-$pkA pens)"
                    )
                )
            }
        }

        events.add(
            MatchEventRecord(
                minute = if (etHome != null) 120 else 90,
                extraMinute = 3,
                type = MatchEventType.PERIOD_MARKER,
                teamId = match.homeTeamId,
                teamName = match.homeTeamName,
                playerName = "",
                description = "Full-Time! Final Score: ${match.homeTeamName} $currentH - $currentA ${match.awayTeamName}" +
                    if (pensHome != null) " ($pensHome-$pensAway on penalties)." else ".",
                runningScore = "$currentH - $currentA"
            )
        )

        // Match Statistics Calculation
        val midGap = (hMid - aMid).coerceIn(-25, 25)
        val hPoss = (50 + (midGap * 0.75).roundToInt() + Random.nextInt(-4, 5)).coerceIn(24, 76)
        val aPoss = 100 - hPoss
        val hShotsOnTarget = max(currentH, (baseHomeLambda * 3.2 + Random.nextInt(0, 4)).roundToInt())
        val aShotsOnTarget = max(currentA, (baseAwayLambda * 3.2 + Random.nextInt(0, 4)).roundToInt())
        val hShots = max(hShotsOnTarget + Random.nextInt(2, 9), currentH + 2)
        val aShots = max(aShotsOnTarget + Random.nextInt(2, 9), currentA + 2)

        val stats = MatchStatsRecord(
            homePossession = hPoss,
            awayPossession = aPoss,
            homeShots = hShots,
            awayShots = aShots,
            homeShotsOnTarget = hShotsOnTarget,
            awayShotsOnTarget = aShotsOnTarget,
            homeXg = ((baseHomeLambda * 100.0).roundToInt() / 100.0),
            awayXg = ((baseAwayLambda * 100.0).roundToInt() / 100.0),
            homeCorners = Random.nextInt(2, 11),
            awayCorners = Random.nextInt(1, 10),
            homeFouls = Random.nextInt(6, 17),
            awayFouls = Random.nextInt(6, 18),
            homeYellowCards = hYellow,
            awayYellowCards = aYellow,
            homeRedCards = hRed,
            awayRedCards = aRed
        )

        return match.copy(
            homeScore = currentH,
            awayScore = currentA,
            homeExtraTimeScore = etHome,
            awayExtraTimeScore = etAway,
            homePenalties = pensHome,
            awayPenalties = pensAway,
            isPlayed = true,
            events = events.sortedWith(compareBy({ it.minute }, { it.extraMinute })),
            stats = stats
        )
    }

    private fun simulatePenaltyShootout(
        homeName: String,
        awayName: String,
        homeGk: Int,
        awayGk: Int
    ): Triple<Int, Int, String> {
        var hPen = 0
        var aPen = 0
        val pProbH = (0.76 - (awayGk - 75) * 0.003).coerceIn(0.62, 0.88)
        val pProbA = (0.75 - (homeGk - 75) * 0.003).coerceIn(0.60, 0.87)

        for (round in 1..5) {
            if (Random.nextDouble() < pProbH) hPen++
            val remA = 5 - round + 1
            val remH = 5 - round
            if (hPen > aPen + remA || aPen > hPen + remH) break
            if (Random.nextDouble() < pProbA) aPen++
            if (hPen > aPen + (5 - round) || aPen > hPen + (5 - round)) break
        }
        var suddenDeathRounds = 0
        while (hPen == aPen && suddenDeathRounds < 15) {
            suddenDeathRounds++
            val hScored = Random.nextDouble() < pProbH
            val aScored = Random.nextDouble() < pProbA
            if (hScored) hPen++
            if (aScored) aPen++
            if (hPen == aPen && suddenDeathRounds == 15) {
                if (Random.nextBoolean()) hPen++ else aPen++
            }
        }
        val winner = if (hPen > aPen) homeName else awayName
        val summary = "120' PENALTY SHOOTOUT: $winner wins $hPen-$aPen on penalties in a nerve-wracking shootout!"
        return Triple(hPen, aPen, summary)
    }

    /**
     * Dedicated simulation logic for Basketball, Tennis, Volleyball, Handball, Rugby, Cricket,
     * Esports, Boxing, MMA, Table Tennis, Badminton, and Baseball.
     */
    private fun simulateOtherSportMatch(
        match: UtsMatch,
        homeTeam: UtsTeam?,
        awayTeam: UtsTeam?,
        randomnessFactor: Double
    ): UtsMatch {
        val hOvr = homeTeam?.overallRating ?: match.homeRating
        val aOvr = awayTeam?.overallRating ?: match.awayRating
        val diff = ((hOvr - aOvr) / max(1.0, randomnessFactor)).toInt()
        val periodScores = mutableListOf<Pair<Int, Int>>()
        var hTotal = 0
        var aTotal = 0

        when (match.sport) {
            SportType.BASKETBALL -> {
                for (q in 1..4) {
                    val hQ = (24 + diff * 0.25 + Random.nextInt(-6, 8)).roundToInt().coerceAtLeast(12)
                    val aQ = (23 - diff * 0.20 + Random.nextInt(-6, 8)).roundToInt().coerceAtLeast(12)
                    periodScores.add(hQ to aQ)
                    hTotal += hQ
                    aTotal += aQ
                }
                if (hTotal == aTotal) {
                    val hOt = Random.nextInt(8, 16)
                    val aOt = if (Random.nextBoolean()) hOt - Random.nextInt(1, 5) else hOt + Random.nextInt(1, 5)
                    periodScores.add(hOt to aOt)
                    hTotal += hOt
                    aTotal += aOt
                }
            }
            SportType.TENNIS, SportType.VOLLEYBALL, SportType.TABLE_TENNIS, SportType.BADMINTON, SportType.ESPORTS -> {
                val setsToWin = if (match.sport == SportType.VOLLEYBALL || match.sport == SportType.TABLE_TENNIS) 3 else 2
                val winProbH = (0.50 + diff * 0.012).coerceIn(0.15, 0.85)
                while (hTotal < setsToWin && aTotal < setsToWin) {
                    if (Random.nextDouble() < winProbH) {
                        hTotal++
                        periodScores.add(1 to 0)
                    } else {
                        aTotal++
                        periodScores.add(0 to 1)
                    }
                }
            }
            SportType.HANDBALL -> {
                for (h in 1..2) {
                    val hHalf = (14 + diff * 0.15 + Random.nextInt(-3, 5)).roundToInt().coerceAtLeast(8)
                    val aHalf = (14 - diff * 0.15 + Random.nextInt(-3, 5)).roundToInt().coerceAtLeast(8)
                    periodScores.add(hHalf to aHalf)
                    hTotal += hHalf
                    aTotal += aHalf
                }
                if (match.requiresWinner && hTotal == aTotal) hTotal++
            }
            SportType.RUGBY -> {
                hTotal = (22 + diff * 0.4 + Random.nextInt(-8, 12)).roundToInt().coerceAtLeast(3)
                aTotal = (20 - diff * 0.4 + Random.nextInt(-8, 12)).roundToInt().coerceAtLeast(3)
                if (match.requiresWinner && hTotal == aTotal) hTotal += 3
            }
            SportType.CRICKET -> {
                hTotal = (165 + diff * 1.2 + Random.nextInt(-30, 45)).roundToInt().coerceAtLeast(85)
                aTotal = (160 - diff * 1.2 + Random.nextInt(-30, 45)).roundToInt().coerceAtLeast(80)
                if (hTotal == aTotal) hTotal++
            }
            SportType.BASEBALL -> {
                hTotal = (4 + diff * 0.08 + Random.nextInt(-3, 5)).roundToInt().coerceAtLeast(0)
                aTotal = (4 - diff * 0.08 + Random.nextInt(-3, 5)).roundToInt().coerceAtLeast(0)
                if (hTotal == aTotal) hTotal++
            }
            SportType.BOXING, SportType.MMA -> {
                hTotal = (114 + (diff * 0.1).roundToInt() + Random.nextInt(-2, 3)).coerceIn(105, 120)
                aTotal = (114 - (diff * 0.1).roundToInt() + Random.nextInt(-2, 3)).coerceIn(105, 120)
                if (hTotal == aTotal) hTotal++
            }
            else -> {}
        }

        val eventSummary = MatchEventRecord(
            minute = 1,
            type = MatchEventType.PERIOD_MARKER,
            teamId = if (hTotal >= aTotal) match.homeTeamId else match.awayTeamId,
            teamName = if (hTotal >= aTotal) match.homeTeamName else match.awayTeamName,
            playerName = match.sport.displayName,
            description = "${match.sport.icon} ${match.sport.displayName} Final Result: ${match.homeTeamName} $hTotal - $aTotal ${match.awayTeamName}",
            runningScore = "$hTotal - $aTotal"
        )

        return match.copy(
            homeScore = hTotal,
            awayScore = aTotal,
            isPlayed = true,
            events = listOf(eventSummary),
            periodScores = periodScores
        )
    }

    private fun samplePoisson(lambda: Double): Int {
        val l = exp(-lambda)
        var k = 0
        var p = 1.0
        do {
            k++
            p *= Random.nextDouble()
        } while (p > l && k < 12)
        return k - 1
    }

    /**
     * Dynamic Team Rating Evolution (Season Mode):
     * Weighs opponent strength so underdog upsets produce meaningful rating jumps.
     */
    fun calculateUpdatedRatings(
        homeTeam: UtsTeam,
        awayTeam: UtsTeam,
        match: UtsMatch
    ): Pair<UtsTeam, UtsTeam> {
        if (!match.isPlayed) return homeTeam to awayTeam
        val expectedHome = 1.0 / (1.0 + 10.0.pow((awayTeam.overallRating - homeTeam.overallRating) / 28.0))
        val actualHome = when {
            match.homeScore > match.awayScore -> 1.0
            match.homeScore < match.awayScore -> 0.0
            else -> 0.5
        }
        val goalMarginMultiplier = 1.0 + (abs(match.homeScore - match.awayScore).coerceAtMost(4) * 0.15)
        val rawDelta = (2.4 * goalMarginMultiplier * (actualHome - expectedHome)).roundToInt()

        val homeDelta = rawDelta.coerceIn(-3, 3)
        val awayDelta = (-rawDelta).coerceIn(-3, 3)

        val newHomeOvr = (homeTeam.overallRating + homeDelta).coerceIn(1, 99)
        val newAwayOvr = (awayTeam.overallRating + awayDelta).coerceIn(1, 99)

        val newHomeForm = when {
            match.homeScore > match.awayScore -> (homeTeam.form + 2).coerceIn(-10, 10)
            match.homeScore < match.awayScore -> (homeTeam.form - 2).coerceIn(-10, 10)
            else -> homeTeam.form
        }
        val newAwayForm = when {
            match.awayScore > match.homeScore -> (awayTeam.form + 2).coerceIn(-10, 10)
            match.awayScore < match.homeScore -> (awayTeam.form - 2).coerceIn(-10, 10)
            else -> awayTeam.form
        }

        val updatedHome = homeTeam.copy(
            overallRating = newHomeOvr,
            attackRating = (homeTeam.attackRating + homeDelta).coerceIn(1, 99),
            midfieldRating = (homeTeam.midfieldRating + homeDelta).coerceIn(1, 99),
            defenseRating = (homeTeam.defenseRating + homeDelta).coerceIn(1, 99),
            form = newHomeForm,
            ratingHistory = (homeTeam.ratingHistory + newHomeOvr).takeLast(25)
        )
        val updatedAway = awayTeam.copy(
            overallRating = newAwayOvr,
            attackRating = (awayTeam.attackRating + awayDelta).coerceIn(1, 99),
            midfieldRating = (awayTeam.midfieldRating + awayDelta).coerceIn(1, 99),
            defenseRating = (awayTeam.defenseRating + awayDelta).coerceIn(1, 99),
            form = newAwayForm,
            ratingHistory = (awayTeam.ratingHistory + newAwayOvr).takeLast(25)
        )
        return updatedHome to updatedAway
    }

    /**
     * Pot-based & Confederation-Protected Tournament Group Draw System
     */
    fun performPotBasedGroupDraw(
        teams: List<UtsTeam>,
        groupsCount: Int,
        teamsPerGroup: Int,
        hostTeamIds: List<String> = emptyList(),
        confederationProtection: Boolean = true
    ): Pair>, List<String>> {
        val groupNames = (0 until groupsCount).map { idx ->
            val letter = ('A' + (idx % 26))
            val suffix = if (idx >= 26) "${(idx / 26) + 1}" else ""
            "Group $letter$suffix"
        }
        val groups = groupNames.associateWith { mutableListOf<UtsTeam>() }.toMutableMap()
        val drawLog = mutableListOf<String>()

        // Sort hosts first, then remaining teams by overall rating descending
        val hosts = teams.filter { it.id in hostTeamIds }
        val others = teams.filter { it.id !in hostTeamIds }.sortedByDescending { it.overallRating }
        val orderedTeams = (hosts + others).take(groupsCount * teamsPerGroup)

        val pots = orderedTeams.chunked(groupsCount)
        pots.forEachIndexed { potIndex, potTeams ->
            drawLog.add("--- POT ${potIndex + 1} DRAW ---")
            val shuffledPot = if (potIndex == 0 && hosts.isNotEmpty()) {
                hosts + potTeams.filter { it !in hosts }.shuffled()
            } else {
                potTeams.shuffled()
            }

            shuffledPot.forEach { team ->
                val targetGroup = groupNames.firstOrNull { gName ->
                    val currentMembers = groups[gName] ?: emptyList()
                    val hasRoom = currentMembers.size <= potIndex && currentMembers.size < teamsPerGroup
                    if (!hasRoom) return@firstOrNull false
                    if (!confederationProtection) return@firstOrNull true
                    val sameConfedCount = currentMembers.count { it.confederation == team.confederation }
                    val maxAllowed = if (team.confederation == Confederation.UEFA) 2 else 1
                    sameConfedCount < maxAllowed
                } ?: groupNames.first { (groups[it]?.size ?: 0) < teamsPerGroup }

                groups[targetGroup]?.add(team)
                drawLog.add("Pot ${potIndex + 1}: ${team.flagEmoji} ${team.name} (${team.overallRating}) drawn into $targetGroup")
            }
        }

        val idMap = groups.mapValues { (_, list) -> list.map { it.id } }
        return idMap to drawLog
    }

    /**
     * Generates complete fixtures for Group stages, Round Robin, Swiss System, or Knockout Rounds.
     */
    fun generateTournamentInitialFixtures(
        tournamentId: String,
        format: TournamentFormat,
        teams: List<UtsTeam>,
        groupsMap: Map<String, List<String>>,
        sport: SportType = SportType.FOOTBALL,
        twoLeggedKnockout: Boolean = false
    ): List<UtsMatch> {
        val teamLookup = teams.associateBy { it.id }
        val matches = mutableListOf<UtsMatch>()
        var matchCounter = 1

        when (format) {
            TournamentFormat.GROUP_PLUS_KNOCKOUT -> {
                groupsMap.forEach { (groupName, memberIds) ->
                    val groupTeams = memberIds.mapNotNull { teamLookup[it] }
                    val schedule = generateRoundRobinPairs(groupTeams, doubleRoundRobin = false)
                    schedule.forEach { (roundIdx, pairs) ->
                        pairs.forEach { (home, away) ->
                            matches.add(
                                UtsMatch(
                                    id = "${tournamentId}_g_${matchCounter++}",
                                    tournamentId = tournamentId,
                                    stageName = groupName,
                                    groupName = groupName,
                                    roundNumber = roundIdx,
                                    homeTeamId = home.id,
                                    homeTeamName = home.name,
                                    homeFlag = home.flagEmoji,
                                    homeRating = home.overallRating,
                                    awayTeamId = away.id,
                                    awayTeamName = away.name,
                                    awayFlag = away.flagEmoji,
                                    awayRating = away.overallRating,
                                    neutralVenue = true,
                                    sport = sport
                                )
                            )
                        }
                    }
                }
            }
            TournamentFormat.ROUND_ROBIN, TournamentFormat.DOUBLE_ROUND_ROBIN, TournamentFormat.SURVIVAL_ELIMINATION -> {
                val doubleRR = format == TournamentFormat.DOUBLE_ROUND_ROBIN
                val schedule = generateRoundRobinPairs(teams, doubleRoundRobin = doubleRR)
                schedule.forEach { (roundIdx, pairs) ->
                    pairs.forEach { (home, away) ->
                        matches.add(
                            UtsMatch(
                                id = "${tournamentId}_rr_${matchCounter++}",
                                tournamentId = tournamentId,
                                stageName = "Matchday $roundIdx",
                                groupName = "League Table",
                                roundNumber = roundIdx,
                                homeTeamId = home.id,
                                homeTeamName = home.name,
                                homeFlag = home.flagEmoji,
                                homeRating = home.overallRating,
                                awayTeamId = away.id,
                                awayTeamName = away.name,
                                awayFlag = away.flagEmoji,
                                awayRating = away.overallRating,
                                neutralVenue = false,
                                venueName = home.stadiumName,
                                sport = sport
                            )
                        )
                    }
                }
            }
            TournamentFormat.SWISS_SYSTEM -> {
                // 8-matchday UEFA Champions League Swiss League Phase
                val shuffled = teams.shuffled()
                val n = shuffled.size
                val rounds = min(8, max(2, n - 1))
                for (r in 1..rounds) {
                    val used = mutableSetOf<String>()
                    for (i in 0 until n / 2) {
                        val h = shuffled[i]
                        val a = shuffled[(i + r) % n]
                        if (h.id != a.id && h.id !in used && a.id !in used) {
                            used.add(h.id)
                            used.add(a.id)
                            val swapHome = r % 2 == 0
                            val actualH = if (swapHome) a else h
                            val actualA = if (swapHome) h else a
                            matches.add(
                                UtsMatch(
                                    id = "${tournamentId}_swiss_${matchCounter++}",
                                    tournamentId = tournamentId,
                                    stageName = "League Phase MD $r",
                                    groupName = "Swiss Table",
                                    roundNumber = r,
                                    homeTeamId = actualH.id,
                                    homeTeamName = actualH.name,
                                    homeFlag = actualH.flagEmoji,
                                    homeRating = actualH.overallRating,
                                    awayTeamId = actualA.id,
                                    awayTeamName = actualA.name,
                                    awayFlag = actualA.flagEmoji,
                                    awayRating = actualA.overallRating,
                                    venueName = actualH.stadiumName,
                                    sport = sport
                                )
                            )
                        }
                    }
                }
            }
            else -> {
                // Direct Knockout / Best-of Series / Battle Royale
                matches.addAll(
                    generateKnockoutRoundMatches(
                        tournamentId = tournamentId,
                        qualifiedTeams = teams,
                        roundNumber = 1,
                        twoLegged = twoLeggedKnockout || format == TournamentFormat.TWO_LEGGED_KNOCKOUT,
                        sport = sport
                    )
                )
            }
        }
        return matches
    }

    fun generateKnockoutRoundMatches(
        tournamentId: String,
        qualifiedTeams: List<UtsTeam>,
        roundNumber: Int,
        twoLegged: Boolean = false,
        sport: SportType = SportType.FOOTBALL
    ): List<UtsMatch> {
        val count = qualifiedTeams.size
        val stageTitle = when {
            count <= 2 -> "Final"
            count <= 4 -> "Semi-Finals"
            count <= 8 -> "Quarter-Finals"
            count <= 16 -> "Round of 16"
            count <= 32 -> "Round of 32"
            count <= 64 -> "Round of 64"
            else -> "Round of 128"
        }
        val list = mutableListOf<UtsMatch>()
        val half = count / 2
        for (i in 0 until half) {
            val home = qualifiedTeams[i]
            val away = qualifiedTeams[count - 1 - i]
            val isFinal = count <= 2
            list.add(
                UtsMatch(
                    id = "${tournamentId}_ko_r${roundNumber}_m${i + 1}_leg1",
                    tournamentId = tournamentId,
                    stageName = stageTitle,
                    roundNumber = roundNumber,
                    legNumber = 1,
                    homeTeamId = home.id,
                    homeTeamName = home.name,
                    homeFlag = home.flagEmoji,
                    homeRating = home.overallRating,
                    awayTeamId = away.id,
                    awayTeamName = away.name,
                    awayFlag = away.flagEmoji,
                    awayRating = away.overallRating,
                    isKnockout = true,
                    requiresWinner = !twoLegged || isFinal,
                    neutralVenue = isFinal,
                    venueName = if (isFinal) "Grand Final Stadium" else home.stadiumName,
                    sport = sport
                )
            )
            if (twoLegged && !isFinal) {
                list.add(
                    UtsMatch(
                        id = "${tournamentId}_ko_r${roundNumber}_m${i + 1}_leg2",
                        tournamentId = tournamentId,
                        stageName = "$stageTitle (2nd Leg)",
                        roundNumber = roundNumber,
                        legNumber = 2,
                        homeTeamId = away.id,
                        homeTeamName = away.name,
                        homeFlag = away.flagEmoji,
                        homeRating = away.overallRating,
                        awayTeamId = home.id,
                        awayTeamName = home.name,
                        awayFlag = home.flagEmoji,
                        awayRating = home.overallRating,
                        isKnockout = true,
                        requiresWinner = true,
                        neutralVenue = false,
                        venueName = away.stadiumName,
                        sport = sport
                    )
                )
            }
        }
        return list
    }

    private fun generateRoundRobinPairs(
        teams: List<UtsTeam>,
        doubleRoundRobin: Boolean
    ): List<Pair<Int, List<Pair<UtsTeam, UtsTeam>>>> {
        if (teams.size < 2) return emptyList()
        val working = teams.toMutableList()
        val dummy = UtsTeam(id = "BYE", name = "BYE", shortCode = "BYE", country = "", flagEmoji = "")
        if (working.size % 2 != 0) working.add(dummy)

        val n = working.size
        val numRounds = n - 1
        val half = n / 2
        val firstHalfSchedule = mutableListOf<Pair<Int, List<Pair<UtsTeam, UtsTeam>>>>()

        for (round in 1..numRounds) {
            val pairs = mutableListOf<Pair<UtsTeam, UtsTeam>>()
            for (i in 0 until half) {
                val t1 = working[i]
                val t2 = working[n - 1 - i]
                if (t1.id != "BYE" && t2.id != "BYE") {
                    if (round % 2 == 1) pairs.add(t1 to t2) else pairs.add(t2 to t1)
                }
            }
            firstHalfSchedule.add(round to pairs)
            // Rotate all except index 0
            val last = working.removeAt(working.size - 1)
            working.add(1, last)
        }

        if (!doubleRoundRobin) return firstHalfSchedule

        val secondHalfSchedule = firstHalfSchedule.map { (r, pairs) ->
            (r + numRounds) to pairs.map { (h, a) -> a to h }
        }
        return firstHalfSchedule + secondHalfSchedule
    }

    /**
     * Computes Group or League Standings with Form Guide and Qualification/Relegation Status Badges.
     */
    fun computeStandings(
        teamIds: List<String>,
        teamLookup: Map<String, UtsTeam>,
        matches: List<UtsMatch>,
        groupName: String,
        pointsWin: Int = 3,
        pointsDraw: Int = 1,
        pointsLoss: Int = 0,
        topQualifiedCount: Int = 2,
        playoffCount: Int = 0,
        relegationCount: Int = 0
    ): List<StandingsRow> {
        val map = teamIds.associateWith { id ->
            val t = teamLookup[id]
            StandingsRow(
                teamId = id,
                teamName = t?.name ?: id,
                flagEmoji = t?.flagEmoji ?: "🏳️",
                groupName = groupName,
                rating = t?.overallRating ?: 75
            )
        }.toMutableMap()

        matches.filter { it.isPlayed && !it.isKnockout }.forEach { m ->
            val hRow = map[m.homeTeamId]
            val aRow = map[m.awayTeamId]
            if (hRow != null && aRow != null) {
                val hWin = m.homeScore > m.awayScore
                val aWin = m.awayScore > m.homeScore
                val hPts = when {
                    hWin -> pointsWin
                    aWin -> pointsLoss
                    else -> pointsDraw
                }
                val aPts = when {
                    aWin -> pointsWin
                    hWin -> pointsLoss
                    else -> pointsDraw
                }
                val hBadge = when {
                    hWin -> "W"
                    aWin -> "L"
                    else -> "D"
                }
                val aBadge = when {
                    aWin -> "W"
                    hWin -> "L"
                    else -> "D"
                }
                map[m.homeTeamId] = hRow.copy(
                    played = hRow.played + 1,
                    won = hRow.won + if (hWin) 1 else 0,
                    drawn = hRow.drawn + if (!hWin && !aWin) 1 else 0,
                    lost = hRow.lost + if (aWin) 1 else 0,
                    goalsFor = hRow.goalsFor + m.homeScore,
                    goalsAgainst = hRow.goalsAgainst + m.awayScore,
                    points = hRow.points + hPts,
                    formGuide = (hRow.formGuide + hBadge).takeLast(5)
                )
                map[m.awayTeamId] = aRow.copy(
                    played = aRow.played + 1,
                    won = aRow.won + if (aWin) 1 else 0,
                    drawn = aRow.drawn + if (!hWin && !aWin) 1 else 0,
                    lost = aRow.lost + if (hWin) 1 else 0,
                    goalsFor = aRow.goalsFor + m.awayScore,
                    goalsAgainst = aRow.goalsAgainst + m.homeScore,
                    points = aRow.points + aPts,
                    formGuide = (aRow.formGuide + aBadge).takeLast(5)
                )
            }
        }

        val sorted = map.values.sortedWith(
            compareByDescending<StandingsRow> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
                .thenByDescending { it.rating }
        )

        val total = sorted.size
        return sorted.mapIndexed { index, row ->
            val badge = when {
                index < topQualifiedCount -> "Q"
                index < topQualifiedCount + playoffCount -> "PO"
                relegationCount > 0 && index >= total - relegationCount -> "REL"
                else -> ""
            }
            row.copy(statusBadge = badge)
        }
    }

    /**
     * Simulates global FIFA World Cup Qualification across all 6 Confederations
     * (UEFA, CAF, AFC, CONMEBOL, CONCACAF, OFC) and returns qualified teams + qualifier fixtures.
     */
    fun generateAndSimulateConfederationQualifiers(
        allNationalTeams: List<UtsTeam>,
        targetFinalsCount: Int,
        hostTeamIds: List<String>
    ): Pair<List<UtsTeam>, List<UtsMatch>> {
        val hosts = allNationalTeams.filter { it.id in hostTeamIds }
        val remainingSlots = (targetFinalsCount - hosts.size).coerceAtLeast(2)
        val pool = allNationalTeams.filter { it.id !in hostTeamIds && it.category == TeamCategory.MENS_NATIONAL }

        val qualified = mutableListOf<UtsTeam>()
        qualified.addAll(hosts)
        val qualMatches = mutableListOf<UtsMatch>()

        val slotRatios = mapOf(
            Confederation.UEFA to 0.34,
            Confederation.CAF to 0.20,
            Confederation.AFC to 0.18,
            Confederation.CONMEBOL to 0.13,
            Confederation.CONCACAF to 0.11,
            Confederation.OFC to 0.04
        )

        Confederation.values().forEach { confed ->
            val confedTeams = pool.filter { it.confederation == confed }.sortedByDescending { it.overallRating }
            val allocated = max(1, (remainingSlots * (slotRatios[confed] ?: 0.1)).roundToInt())
            val groupSize = if (confed == Confederation.CONMEBOL) min(10, confedTeams.size) else 5
            val groups = confedTeams.chunked(max(3, groupSize))

            val groupWinners = mutableListOf<UtsTeam>()
            groups.forEachIndexed { gIdx, gTeams ->
                val gName = "${confed.code} Qualifiers Grp ${'A' + gIdx}"
                val pairs = generateRoundRobinPairs(gTeams, doubleRoundRobin = false)
                val gMatches = mutableListOf<UtsMatch>()
                pairs.forEach { (r, roundPairs) ->
                    roundPairs.forEach { (h, a) ->
                        val m = simulateMatch(
                            UtsMatch(
                                id = "qual_${confed.code}_${gIdx}_${r}_${h.id}_${a.id}",
                                tournamentId = "wc_qualifiers",
                                stageName = gName,
                                groupName = gName,
                                roundNumber = r,
                                homeTeamId = h.id,
                                homeTeamName = h.name,
                                homeFlag = h.flagEmoji,
                                homeRating = h.overallRating,
                                awayTeamId = a.id,
                                awayTeamName = a.name,
                                awayFlag = a.flagEmoji,
                                awayRating = a.overallRating,
                                venueName = h.stadiumName
                            ),
                            h,
                            a
                        )
                        gMatches.add(m)
                    }
                }
                qualMatches.addAll(gMatches)
                val table = computeStandings(
                    teamIds = gTeams.map { it.id },
                    teamLookup = gTeams.associateBy { it.id },
                    matches = gMatches,
                    groupName = gName
                )
                val takeCount = if (confed == Confederation.CONMEBOL) min(allocated, table.size) else 1
                table.take(takeCount).forEach { row ->
                    confedTeams.find { it.id == row.teamId }?.let { groupWinners.add(it) }
                }
            }
            val picked = (groupWinners + confedTeams.filter { it !in groupWinners }).take(allocated)
            qualified.addAll(picked)
        }

        val finalQualified = (qualified + pool.filter { it !in qualified }.sortedByDescending { it.overallRating })
            .distinctBy { it.id }
            .take(targetFinalsCount)

        return finalQualified to qualMatches
    }
}
