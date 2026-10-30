package com.uts.simulator.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uts.simulator.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.max
import kotlin.math.min

enum class MainNavTab(val label: String, val icon: String) {
    HOME("Home", "🏠"),
    INTERNATIONAL("International", "🌍"),
    CLUBS("Clubs", "🛡️"),
    SEASON_MODE("Season Mode", "🏆"),
    MORE("More", "⚙️")
}

data class UtsUiState(
    val selectedTab: MainNavTab = MainNavTab.HOME,
    val showOnboarding: Boolean = false,
    val allTeams: List<UtsTeam> = emptyList(),
    val historicalWorldCups: List<HistoricalWorldCupEdition> = emptyList(),
    val historicalClubSeasons: List<HistoricalClubSeasonRecord> = emptyList(),
    val savedTournaments: List<TournamentInstance> = emptyList(),
    val activeTournamentId: String? = null,
    val activeMatchForCenter: UtsMatch? = null,
    val seasonMode: SeasonModeState = SeasonModeState(),
    val simSpeed: SimSpeed = SimSpeed.INSTANT,
    val randomnessLevel: Double = 1.0, // 0.6 Low, 1.0 Realistic, 1.5 Chaos
    val statusBannerMessage: String? = null,
    val isLiveMatchAnimating: Boolean = false,
    val liveMatchMinute: Int = 0
)

class UtsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UtsUiState())
    val uiState: StateFlow<UtsUiState> = _uiState.asStateFlow()

    private val storageFile: File
        get() = File(getApplication<Application>().filesDir, "uts_persistent_state_v1.json")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            initializeCompleteDatabaseAndState()
        }
    }

    private fun initializeCompleteDatabaseAndState() {
        val defaultTeams = buildAllNationalAndClubTeams()
        val historicalWcs = buildVerifiedWorldCupHistory()
        val historicalClubs = buildVerifiedClubSeasonsHistory()
        val initialTournaments = buildPreloadedTournaments(defaultTeams)
        val initialSeasonMode = buildDefaultSeasonPyramids(defaultTeams)

        if (storageFile.exists()) {
            try {
                val raw = storageFile.readText()
                val root = JSONObject(raw)
                val loadedTeams = parseTeamsJson(root.optJSONArray("customTeams"), defaultTeams)
                val loadedTournaments = parseTournamentsJson(root.optJSONArray("tournaments"), initialTournaments)
                _uiState.update {
                    it.copy(
                        allTeams = loadedTeams,
                        historicalWorldCups = historicalWcs,
                        historicalClubSeasons = historicalClubs,
                        savedTournaments = loadedTournaments,
                        activeTournamentId = loadedTournaments.firstOrNull()?.id,
                        seasonMode = initialSeasonMode
                    )
                }
                return
            } catch (_: Exception) {
                // Fallback to fresh verified seed if file is corrupted
            }
        }

        _uiState.update {
            it.copy(
                allTeams = defaultTeams,
                historicalWorldCups = historicalWcs,
                historicalClubSeasons = historicalClubs,
                savedTournaments = initialTournaments,
                activeTournamentId = initialTournaments.firstOrNull()?.id,
                seasonMode = initialSeasonMode
            )
        }
        persistStateToDisk()
    }

    private fun persistStateToDisk() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val state = _uiState.value
                val root = JSONObject()
                val teamsArr = JSONArray()
                state.allTeams.forEach { t ->
                    val o = JSONObject()
                    o.put("id", t.id)
                    o.put("name", t.name)
                    o.put("shortCode", t.shortCode)
                    o.put("country", t.country)
                    o.put("flagEmoji", t.flagEmoji)
                    o.put("customLogoUri", t.customLogoUri ?: "")
                    o.put("confederation", t.confederation.name)
                    o.put("category", t.category.name)
                    o.put("sport", t.sport.name)
                    o.put("overallRating", t.overallRating)
                    o.put("attackRating", t.attackRating)
                    o.put("midfieldRating", t.midfieldRating)
                    o.put("defenseRating", t.defenseRating)
                    o.put("goalkeeperRating", t.goalkeeperRating)
                    o.put("stadiumName", t.stadiumName)
                    o.put("primaryColorHex", t.primaryColorHex)
                    o.put("domesticTier", t.domesticTier)
                    teamsArr.put(o)
                }
                root.put("customTeams", teamsArr)

                val tourneyArr = JSONArray()
                state.savedTournaments.forEach { tr ->
                    val o = JSONObject()
                    o.put("id", tr.id)
                    o.put("name", tr.name)
                    o.put("subtitle", tr.subtitle)
                    o.put("yearLabel", tr.yearLabel)
                    o.put("sport", tr.sport.name)
                    o.put("category", tr.category.name)
                    o.put("format", tr.format.name)
                    o.put("dataSource", tr.dataSource.name)
                    o.put("teamsCount", tr.teamsCount)
                    o.put("groupsCount", tr.groupsCount)
                    o.put("teamsPerGroup", tr.teamsPerGroup)
                    o.put("advancePerGroup", tr.advancePerGroup)
                    o.put("bestThirdPlacedCount", tr.bestThirdPlacedCount)
                    o.put("isCompleted", tr.isCompleted)
                    o.put("championName", tr.championName ?: "")
                    o.put("runnerUpName", tr.runnerUpName ?: "")
                    o.put("thirdPlaceName", tr.thirdPlaceName ?: "")
                    o.put("participantTeamIds", JSONArray(tr.participantTeamIds))
                    tourneyArr.put(o)
                }
                root.put("tournaments", tourneyArr)
                storageFile.writeText(root.toString())
            } catch (_: Exception) {
            }
        }
    }

    private fun parseTeamsJson(arr: JSONArray?, fallback: List<UtsTeam>): List<UtsTeam> {
        if (arr == null || arr.length() == 0) return fallback
        val list = mutableListOf<UtsTeam>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val logo = o.optString("customLogoUri", "").takeIf { it.isNotBlank() }
            list.add(
                UtsTeam(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    shortCode = o.optString("shortCode", "UTS"),
                    country = o.optString("country", "International"),
                    flagEmoji = o.optString("flagEmoji", "⚽"),
                    customLogoUri = logo,
                    confederation = runCatching { Confederation.valueOf(o.optString("confederation")) }.getOrDefault(Confederation.UEFA),
                    category = runCatching { TeamCategory.valueOf(o.optString("category")) }.getOrDefault(TeamCategory.MENS_NATIONAL),
                    sport = runCatching { SportType.valueOf(o.optString("sport")) }.getOrDefault(SportType.FOOTBALL),
                    overallRating = o.optInt("overallRating", 75),
                    attackRating = o.optInt("attackRating", 75),
                    midfieldRating = o.optInt("midfieldRating", 75),
                    defenseRating = o.optInt("defenseRating", 75),
                    goalkeeperRating = o.optInt("goalkeeperRating", 75),
                    stadiumName = o.optString("stadiumName", "National Stadium"),
                    primaryColorHex = o.optString("primaryColorHex", "#35D07F"),
                    domesticTier = o.optInt("domesticTier", 1)
                )
            )
        }
        return list.ifEmpty { fallback }
    }

    private fun parseTournamentsJson(arr: JSONArray?, fallback: List<TournamentInstance>): List<TournamentInstance> {
        if (arr == null || arr.length() == 0) return fallback
        return fallback
    }

    fun selectTab(tab: MainNavTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    fun postBanner(message: String) {
        _uiState.update { it.copy(statusBannerMessage = message) }
    }

    fun setSimSettings(speed: SimSpeed, randomness: Double) {
        _uiState.update { it.copy(simSpeed = speed, randomnessLevel = randomness, statusBannerMessage = "Simulation settings updated: ${speed.label}") }
    }

    fun openMatchCenter(match: UtsMatch?) {
        _uiState.update { it.copy(activeMatchForCenter = match, isLiveMatchAnimating = false, liveMatchMinute = if (match?.isPlayed == true) 90 else 0) }
    }

    fun selectActiveTournament(tournamentId: String) {
        _uiState.update { it.copy(activeTournamentId = tournamentId) }
    }

    // =========================================================================
    // CUSTOM WORLD CUP, LEAGUE, AND UNIVERSAL TOURNAMENT CREATION
    // =========================================================================

    fun createCustomTournament(
        name: String,
        yearLabel: String,
        hostCountries: List<String>,
        sport: SportType,
        category: TeamCategory,
        format: TournamentFormat,
        participantTeams: List<UtsTeam>,
        groupsCount: Int,
        teamsPerGroup: Int,
        advancePerGroup: Int,
        bestThirdPlacedCount: Int,
        hasThirdPlaceMatch: Boolean,
        twoLeggedKnockout: Boolean,
        runWorldCupQualifiersFirst: Boolean = false
    ) {
        val tourneyId = "tourney_${System.currentTimeMillis()}"
        var finalParticipants = participantTeams
        var qualMatches = emptyList<UtsMatch>()

        if (runWorldCupQualifiersFirst && category == TeamCategory.MENS_NATIONAL) {
            val allNations = _uiState.value.allTeams.filter { it.category == TeamCategory.MENS_NATIONAL }
            val hostIds = allNations.filter { it.name in hostCountries }.map { it.id }
            val (qualified, qMatches) = SimulationEngine.generateAndSimulateConfederationQualifiers(
                allNationalTeams = allNations,
                targetFinalsCount = participantTeams.size.coerceAtLeast(8),
                hostTeamIds = hostIds
            )
            finalParticipants = qualified
            qualMatches = qMatches
        }

        val hostIds = finalParticipants.filter { it.name in hostCountries }.map { it.id }
        val (groupsMap, drawLog) = if (format == TournamentFormat.GROUP_PLUS_KNOCKOUT) {
            SimulationEngine.performPotBasedGroupDraw(
                teams = finalParticipants,
                groupsCount = groupsCount.coerceAtLeast(1),
                teamsPerGroup = teamsPerGroup.coerceAtLeast(2),
                hostTeamIds = hostIds,
                confederationProtection = category == TeamCategory.MENS_NATIONAL
            )
        } else {
            mapOf("League Table" to finalParticipants.map { it.id }) to listOf("Direct ${format.label} Seeding Complete (${finalParticipants.size} teams)")
        }

        val initialMatches = SimulationEngine.generateTournamentInitialFixtures(
            tournamentId = tourneyId,
            format = format,
            teams = finalParticipants,
            groupsMap = groupsMap,
            sport = sport,
            twoLeggedKnockout = twoLeggedKnockout
        )

        val newTourney = TournamentInstance(
            id = tourneyId,
            name = name.ifBlank { "Custom ${sport.displayName} Cup" },
            subtitle = "${format.label} • ${finalParticipants.size} Teams",
            yearLabel = yearLabel,
            hostCountries = hostCountries.ifEmpty { listOf("International") },
            sport = sport,
            category = category,
            format = format,
            dataSource = DataSourceType.USER_CUSTOM,
            teamsCount = finalParticipants.size,
            groupsCount = groupsCount,
            teamsPerGroup = teamsPerGroup,
            advancePerGroup = advancePerGroup,
            bestThirdPlacedCount = bestThirdPlacedCount,
            hasThirdPlaceMatch = hasThirdPlaceMatch,
            twoLeggedKnockout = twoLeggedKnockout,
            participantTeamIds = finalParticipants.map { it.id },
            groupsMap = groupsMap,
            matches = initialMatches,
            drawHistoryLog = drawLog,
            currentPhase = if (format == TournamentFormat.GROUP_PLUS_KNOCKOUT) "Group Stage" else format.label,
            qualificationCompleted = true,
            qualificationMatches = qualMatches
        )

        _uiState.update { state ->
            state.copy(
                savedTournaments = listOf(newTourney) + state.savedTournaments,
                activeTournamentId = newTourney.id,
                statusBannerMessage = "Created '${newTourney.name}' with ${finalParticipants.size} teams & ${initialMatches.size} fixtures!"
            )
        }
        persistStateToDisk()
    }

    /**
     * Recreates any Historical World Cup (1930-2026) or Club Competition Season in What-If Mode.
     */
    fun recreateHistoricalWorldCup(edition: HistoricalWorldCupEdition, whatIfRandomSwap: Boolean = false) {
        val state = _uiState.value
        val pool = state.allTeams.filter { it.category == TeamCategory.MENS_NATIONAL }.sortedByDescending { it.overallRating }
        val count = edition.teamsCount.coerceIn(8, 64)
        val selectedTeams = if (whatIfRandomSwap) {
            pool.shuffled().take(count)
        } else {
            val priorityNames = listOf(edition.championName, edition.runnerUpName, edition.thirdPlaceName, edition.fourthPlaceName)
            val topSeeds = pool.filter { it.name in priorityNames }
            (topSeeds + pool.filter { it !in topSeeds }).take(count)
        }

        val gCount = (count / 4).coerceAtLeast(2)
        createCustomTournament(
            name = "${edition.year} FIFA ${edition.category.label} (${if (whatIfRandomSwap) "What-If Mode" else "Historical Recreation"})",
            yearLabel = edition.year.toString(),
            hostCountries = listOf(edition.hostCountries),
            sport = SportType.FOOTBALL,
            category = edition.category,
            format = TournamentFormat.GROUP_PLUS_KNOCKOUT,
            participantTeams = selectedTeams,
            groupsCount = gCount,
            teamsPerGroup = 4,
            advancePerGroup = 2,
            bestThirdPlacedCount = if (count == 24) 4 else if (count == 48) 8 else 0,
            hasThirdPlaceMatch = true,
            twoLeggedKnockout = false,
            runWorldCupQualifiersFirst = false
        )
    }

    fun recreateHistoricalClubSeason(record: HistoricalClubSeasonRecord) {
        val state = _uiState.value
        val confedClubs = state.allTeams.filter {
            it.category == TeamCategory.MENS_CLUB &&
                (it.confederation == record.confederation || record.country == "World")
        }.sortedByDescending { it.overallRating }

        val participants = if (confedClubs.size >= 16) confedClubs.take(32) else state.allTeams.filter { it.category == TeamCategory.MENS_CLUB }.take(16)
        val useSwiss = record.seasonLabel in listOf("2024-25", "2025-26", "2026-27") && record.competitionName.contains("Champions League")

        createCustomTournament(
            name = "${record.competitionName} ${record.seasonLabel}",
            yearLabel = record.seasonLabel,
            hostCountries = listOf(record.country),
            sport = SportType.FOOTBALL,
            category = TeamCategory.MENS_CLUB,
            format = if (useSwiss) TournamentFormat.SWISS_SYSTEM else TournamentFormat.GROUP_PLUS_KNOCKOUT,
            participantTeams = participants,
            groupsCount = (participants.size / 4).coerceAtLeast(2),
            teamsPerGroup = 4,
            advancePerGroup = 2,
            bestThirdPlacedCount = 0,
            hasThirdPlaceMatch = false,
            twoLeggedKnockout = true
        )
    }

    // =========================================================================
    // MATCH & TOURNAMENT SIMULATION ACTIONS
    // =========================================================================

    fun simulateSingleTournamentMatch(tournamentId: String, matchId: String, openAnimatedView: Boolean = false) {
        val state = _uiState.value
        val tourney = state.savedTournaments.find { it.id == tournamentId } ?: return
        val match = tourney.matches.find { it.id == matchId } ?: return
        val teamMap = state.allTeams.associateBy { it.id }

        val simulated = SimulationEngine.simulateMatch(
            match = match,
            homeTeam = teamMap[match.homeTeamId],
            awayTeam = teamMap[match.awayTeamId],
            randomnessFactor = state.randomnessLevel
        )

        val updatedMatches = tourney.matches.map { if (it.id == matchId) simulated else it }
        val progressedTourney = checkAndAdvanceTournamentStage(tourney.copy(matches = updatedMatches), teamMap)

        _uiState.update { s ->
            s.copy(
                savedTournaments = s.savedTournaments.map { if (it.id == tournamentId) progressedTourney else it },
                activeMatchForCenter = if (openAnimatedView || s.activeMatchForCenter?.id == matchId) simulated else s.activeMatchForCenter
            )
        }

        if (openAnimatedView) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLiveMatchAnimating = true, liveMatchMinute = 1) }
                val stepDelay = state.simSpeed.delayMs.coerceAtLeast(15L)
                for (m in 1..90 step 3) {
                    delay(stepDelay)
                    _uiState.update { it.copy(liveMatchMinute = m) }
                }
                _uiState.update { it.copy(isLiveMatchAnimating = false, liveMatchMinute = 90) }
            }
        }
        persistStateToDisk()
    }

    fun simulateNextRoundInTournament(tournamentId: String) {
        val state = _uiState.value
        val tourney = state.savedTournaments.find { it.id == tournamentId } ?: return
        val teamMap = state.allTeams.associateBy { it.id }
        val unplayed = tourney.matches.filter { !it.isPlayed }
        if (unplayed.isEmpty()) return

        val targetStage = unplayed.first().stageName
        val updatedMatches = tourney.matches.map { m ->
            if (!m.isPlayed && m.stageName == targetStage) {
                SimulationEngine.simulateMatch(m, teamMap[m.homeTeamId], teamMap[m.awayTeamId], state.randomnessLevel)
            } else m
        }

        val progressed = checkAndAdvanceTournamentStage(tourney.copy(matches = updatedMatches), teamMap)
        _uiState.update { s ->
            s.copy(
                savedTournaments = s.savedTournaments.map { if (it.id == tournamentId) progressed else it },
                statusBannerMessage = "Simulated $targetStage in ${progressed.name}!"
            )
        }
        persistStateToDisk()
    }

    fun simulateEntireTournament(tournamentId: String) {
        var safetyCounter = 0
        while (safetyCounter < 15) {
            safetyCounter++
            val state = _uiState.value
            val tourney = state.savedTournaments.find { it.id == tournamentId } ?: break
            if (tourney.isCompleted || tourney.matches.all { it.isPlayed }) break
            val teamMap = state.allTeams.associateBy { it.id }

            val allSimulated = tourney.matches.map { m ->
                if (!m.isPlayed) {
                    SimulationEngine.simulateMatch(m, teamMap[m.homeTeamId], teamMap[m.awayTeamId], state.randomnessLevel)
                } else m
            }
            val progressed = checkAndAdvanceTournamentStage(tourney.copy(matches = allSimulated), teamMap)
            _uiState.update { s ->
                s.copy(savedTournaments = s.savedTournaments.map { if (it.id == tournamentId) progressed else it })
            }
            if (progressed.isCompleted) break
        }
        val finished = _uiState.value.savedTournaments.find { it.id == tournamentId }
        if (finished != null) {
            _uiState.update {
                it.copy(statusBannerMessage = "🏆 ${finished.name} Completed! Champion: ${finished.championName ?: "Decided"}")
            }
        }
        persistStateToDisk()
    }

    private fun checkAndAdvanceTournamentStage(
        tourney: TournamentInstance,
        teamMap: Map<String, UtsTeam>
    ): TournamentInstance {
        if (tourney.matches.any { !it.isPlayed }) return tourney

        // All current matches are played. Check if we transition from Group/Swiss to Knockout, or advance Knockout round.
        val knockoutMatches = tourney.matches.filter { it.isKnockout }

        if (knockoutMatches.isEmpty()) {
            // If Round Robin only, crown #1 on table
            if (tourney.format == TournamentFormat.ROUND_ROBIN || tourney.format == TournamentFormat.DOUBLE_ROUND_ROBIN) {
                val table = SimulationEngine.computeStandings(
                    teamIds = tourney.participantTeamIds,
                    teamLookup = teamMap,
                    matches = tourney.matches,
                    groupName = "League Table",
                    pointsWin = tourney.pointsForWin,
                    pointsDraw = tourney.pointsForDraw
                )
                val champ = table.getOrNull(0)
                val runner = table.getOrNull(1)
                val third = table.getOrNull(2)
                return tourney.copy(
                    isCompleted = true,
                    currentPhase = "Completed",
                    championTeamId = champ?.teamId,
                    championName = "${champ?.flagEmoji ?: ""} ${champ?.teamName ?: ""}".trim(),
                    runnerUpName = "${runner?.flagEmoji ?: ""} ${runner?.teamName ?: ""}".trim(),
                    thirdPlaceName = "${third?.flagEmoji ?: ""} ${third?.teamName ?: ""}".trim()
                )
            }

            // Transition from Group Stage or Swiss League Phase to Knockout Stage
            val qualifiedTeams = mutableListOf<UtsTeam>()
            val thirdPlaceCandidates = mutableListOf<StandingsRow>()

            tourney.groupsMap.forEach { (gName, memberIds) ->
                val table = SimulationEngine.computeStandings(
                    teamIds = memberIds,
                    teamLookup = teamMap,
                    matches = tourney.matches.filter { it.groupName == gName },
                    groupName = gName,
                    topQualifiedCount = tourney.advancePerGroup
                )
                val takeCount = if (tourney.format == TournamentFormat.SWISS_SYSTEM) min(16, table.size) else tourney.advancePerGroup
                table.take(takeCount).forEach { row ->
                    teamMap[row.teamId]?.let { qualifiedTeams.add(it) }
                }
                if (tourney.bestThirdPlacedCount > 0 && table.size >= 3) {
                    thirdPlaceCandidates.add(table[2])
                }
            }

            if (tourney.bestThirdPlacedCount > 0) {
                thirdPlaceCandidates.sortedWith(
                    compareByDescending<StandingsRow> { it.points }
                        .thenByDescending { it.goalDifference }
                        .thenByDescending { it.goalsFor }
                ).take(tourney.bestThirdPlacedCount).forEach { row ->
                    teamMap[row.teamId]?.let { qualifiedTeams.add(it) }
                }
            }

            // Ensure power-of-2 bracket size (2, 4, 8, 16, 32, 64)
            val bracketSize = listOf(64, 32, 16, 8, 4, 2).firstOrNull { it <= qualifiedTeams.size } ?: 2
            val seededKnockoutTeams = qualifiedTeams.take(bracketSize)

            val koMatches = SimulationEngine.generateKnockoutRoundMatches(
                tournamentId = tourney.id,
                qualifiedTeams = seededKnockoutTeams,
                roundNumber = 10,
                twoLegged = tourney.twoLeggedKnockout,
                sport = tourney.sport
            )
            return tourney.copy(
                matches = tourney.matches + koMatches,
                currentPhase = koMatches.firstOrNull()?.stageName ?: "Knockout Stage"
            )
        } else {
            // Advance existing Knockout Round
            val maxRound = knockoutMatches.maxOf { it.roundNumber }
            val latestRoundMatches = knockoutMatches.filter { it.roundNumber == maxRound && it.stageName != "Third Place Playoff" }

            // Group two-legged ties or single matches
            val winners = mutableListOf<UtsTeam>()
            val losers = mutableListOf<UtsTeam>()

            val firstLegMatches = latestRoundMatches.filter { it.legNumber == 1 }
            firstLegMatches.forEach { leg1 ->
                val leg2 = latestRoundMatches.find {
                    it.legNumber == 2 && it.homeTeamId == leg1.awayTeamId && it.awayTeamId == leg1.homeTeamId
                }
                val winnerId = if (leg2 == null) {
                    leg1.winnerTeamId() ?: leg1.homeTeamId
                } else {
                    val aggTeam1 = leg1.homeScore + leg2.awayScore
                    val aggTeam2 = leg1.awayScore + leg2.homeScore
                    when {
                        aggTeam1 > aggTeam2 -> leg1.homeTeamId
                        aggTeam2 > aggTeam1 -> leg1.awayTeamId
                        else -> leg2.winnerTeamId() ?: leg1.homeTeamId
                    }
                }
                val loserId = if (winnerId == leg1.homeTeamId) leg1.awayTeamId else leg1.homeTeamId
                teamMap[winnerId]?.let { winners.add(it) }
                teamMap[loserId]?.let { losers.add(it) }
            }

            if (winners.size <= 1) {
                val champ = winners.firstOrNull()
                val runner = losers.firstOrNull()
                val thirdMatch = knockoutMatches.find { it.stageName == "Third Place Playoff" }
                val thirdTeam = thirdMatch?.winnerTeamId()?.let { teamMap[it] }
                return tourney.copy(
                    isCompleted = true,
                    currentPhase = "Tournament Completed",
                    championTeamId = champ?.id,
                    championName = if (champ != null) "${champ.flagEmoji} ${champ.name}" else "Champion",
                    runnerUpName = if (runner != null) "${runner.flagEmoji} ${runner.name}" else "Runner-Up",
                    thirdPlaceName = if (thirdTeam != null) "${thirdTeam.flagEmoji} ${thirdTeam.name}" else tourney.thirdPlaceName
                )
            }

            val nextRoundNumber = maxRound + 1
            val nextMatches = mutableListOf<UtsMatch>()

            // If Semi-Finals just finished (winners.size == 2) and 3rd-place match is enabled
            if (winners.size == 2 && losers.size == 2 && tourney.hasThirdPlaceMatch) {
                nextMatches.add(
                    UtsMatch(
                        id = "${tourney.id}_ko_3rd_place",
                        tournamentId = tourney.id,
                        stageName = "Third Place Playoff",
                        roundNumber = nextRoundNumber,
                        homeTeamId = losers[0].id,
                        homeTeamName = losers[0].name,
                        homeFlag = losers[0].flagEmoji,
                        homeRating = losers[0].overallRating,
                        awayTeamId = losers[1].id,
                        awayTeamName = losers[1].name,
                        awayFlag = losers[1].flagEmoji,
                        awayRating = losers[1].overallRating,
                        isKnockout = true,
                        requiresWinner = true,
                        neutralVenue = true,
                        venueName = "Bronze Medal Stadium",
                        sport = tourney.sport
                    )
                )
            }

            nextMatches.addAll(
                SimulationEngine.generateKnockoutRoundMatches(
                    tournamentId = tourney.id,
                    qualifiedTeams = winners,
                    roundNumber = nextRoundNumber,
                    twoLegged = tourney.twoLeggedKnockout && winners.size > 2,
                    sport = tourney.sport
                )
            )

            return tourney.copy(
                matches = tourney.matches + nextMatches,
                currentPhase = nextMatches.lastOrNull()?.stageName ?: "Knockout Stage"
            )
        }
    }

    fun deleteTournament(tournamentId: String) {
        _uiState.update { state ->
            val filtered = state.savedTournaments.filterNot { it.id == tournamentId }
            state.copy(
                savedTournaments = filtered,
                activeTournamentId = if (state.activeTournamentId == tournamentId) filtered.firstOrNull()?.id else state.activeTournamentId,
                statusBannerMessage = "Tournament removed."
            )
        }
        persistStateToDisk()
    }

    fun duplicateTournament(tournamentId: String) {
        val orig = _uiState.value.savedTournaments.find { it.id == tournamentId } ?: return
        val copyId = "tourney_copy_${System.currentTimeMillis()}"
        val resetMatches = orig.matches.filter { it.roundNumber <= 3 || !it.isKnockout }.map {
            it.copy(
                id = "${copyId}_${it.id}",
                tournamentId = copyId,
                homeScore = 0,
                awayScore = 0,
                homeExtraTimeScore = null,
                awayExtraTimeScore = null,
                homePenalties = null,
                awayPenalties = null,
                isPlayed = false,
                events = emptyList()
            )
        }
        val duplicated = orig.copy(
            id = copyId,
            name = "${orig.name} (Copy)",
            matches = resetMatches,
            isCompleted = false,
            championName = null,
            runnerUpName = null,
            thirdPlaceName = null,
            currentPhase = "Group Stage"
        )
        _uiState.update { state ->
            state.copy(
                savedTournaments = listOf(duplicated) + state.savedTournaments,
                activeTournamentId = duplicated.id,
                statusBannerMessage = "Duplicated '${orig.name}' with fresh fixtures!"
            )
        }
        persistStateToDisk()
    }

    // =========================================================================
    // SEASON MODE: PYRAMIDS, PROMOTION/RELEGATION & CONTINENTAL QUALIFICATION
    // =========================================================================

    fun selectSeasonCountry(country: String) {
        _uiState.update { it.copy(seasonMode = it.seasonMode.copy(activeCountry = country)) }
    }

    fun toggleDynamicRatings(enabled: Boolean) {
        _uiState.update { it.copy(seasonMode = it.seasonMode.copy(dynamicRatingsEnabled = enabled)) }
    }

    fun simulateSeasonMatchday(matchdaysToAdvance: Int = 1) {
        val state = _uiState.value
        val season = state.seasonMode
        val teamMap = state.allTeams.associateBy { it.id }.toMutableMap()

        val updatedDivisions = season.divisions.map { div ->
            val unplayedRounds = div.matches.filter { !it.isPlayed }.map { it.roundNumber }.distinct().sorted()
            val targetRounds = unplayedRounds.take(matchdaysToAdvance).toSet()
            if (targetRounds.isEmpty()) return@map div

            val newMatches = div.matches.map { m ->
                if (!m.isPlayed && m.roundNumber in targetRounds) {
                    val hTeam = teamMap[m.homeTeamId]
                    val aTeam = teamMap[m.awayTeamId]
                    val sim = SimulationEngine.simulateMatch(m, hTeam, aTeam, state.randomnessLevel)
                    if (season.dynamicRatingsEnabled && hTeam != null && aTeam != null) {
                        val (newH, newA) = SimulationEngine.calculateUpdatedRatings(hTeam, aTeam, sim)
                        teamMap[newH.id] = newH
                        teamMap[newA.id] = newA
                    }
                    sim
                } else m
            }
            div.copy(matches = newMatches)
        }

        // Also advance domestic & continental cups in Season Mode
        val updatedDomesticCups = season.domesticCups.map { cup ->
            val unplayed = cup.matches.filter { !it.isPlayed }
            if (unplayed.isEmpty()) cup else {
                val stage = unplayed.first().stageName
                val simMatches = cup.matches.map { m ->
                    if (!m.isPlayed && m.stageName == stage) {
                        SimulationEngine.simulateMatch(m, teamMap[m.homeTeamId], teamMap[m.awayTeamId], state.randomnessLevel)
                    } else m
                }
                checkAndAdvanceTournamentStage(cup.copy(matches = simMatches), teamMap)
            }
        }

        val updatedContinentalCups = season.continentalCups.map { cup ->
            val unplayed = cup.matches.filter { !it.isPlayed }
            if (unplayed.isEmpty()) cup else {
                val stage = unplayed.first().stageName
                val simMatches = cup.matches.map { m ->
                    if (!m.isPlayed && m.stageName == stage) {
                        SimulationEngine.simulateMatch(m, teamMap[m.homeTeamId], teamMap[m.awayTeamId], state.randomnessLevel)
                    } else m
                }
                checkAndAdvanceTournamentStage(cup.copy(matches = simMatches), teamMap)
            }
        }

        _uiState.update {
            it.copy(
                allTeams = teamMap.values.toList(),
                seasonMode = season.copy(
                    divisions = updatedDivisions,
                    domesticCups = updatedDomesticCups,
                    continentalCups = updatedContinentalCups,
                    currentMatchday = (season.currentMatchday + matchdaysToAdvance).coerceAtMost(season.totalMatchdays)
                ),
                statusBannerMessage = "Simulated $matchdaysToAdvance matchday(s) across all pyramids & cups!"
            )
        }
        persistStateToDisk()
    }

    /**
     * Completes the current season, executes automatic Promotion & Relegation across all tiers,
     * qualifies top clubs for next season's UEFA / CAF / CONMEBOL Continental Cups,
     * and generates fresh fixtures for the new season year.
     */
    fun finishAndAdvanceToNextSeason() {
        // Ensure all remaining matches in current season are simulated
        simulateSeasonMatchday(matchdaysToAdvance = 50)

        val state = _uiState.value
        val season = state.seasonMode
        val teamMap = state.allTeams.associateBy { it.id }
        val nextYear = season.seasonYear + 1
        val logs = season.seasonHistoryLogs.toMutableList()
        val uclQualifiers = mutableListOf<UtsTeam>()
        val cafClQualifiers = mutableListOf<UtsTeam>()

        val divisionsByCountry = season.divisions.groupBy { it.country }
        val nextSeasonDivisions = mutableListOf<DivisionModel>()

        divisionsByCountry.forEach { (country, countryDivs) ->
            val sortedDivs = countryDivs.sortedBy { it.tierLevel }
            val standingsPerTier = sortedDivs.associate { div ->
                val table = SimulationEngine.computeStandings(
                    teamIds = div.teamIds,
                    teamLookup = teamMap,
                    matches = div.matches,
                    groupName = div.name,
                    topQualifiedCount = if (div.tierLevel == 1) div.championsLeagueSlots else div.autoPromotionSlots,
                    playoffCount = if (div.tierLevel == 1) div.secondaryContinentalSlots else div.playoffPromotionSlots,
                    relegationCount = div.relegationSlots
                )
                div.tierLevel to table
            }

            // Record Tier 1 Champion & Continental Qualifiers
            standingsPerTier[1]?.let { topTable ->
                val champ = topTable.firstOrNull()
                if (champ != null) {
                    logs.add("Season ${season.seasonYear}/${(season.seasonYear + 1) % 100} • $country Champion: ${champ.flagEmoji} ${champ.teamName} (${champ.points} pts)")
                }
                topTable.take(4).mapNotNull { teamMap[it.teamId] }.forEach { club ->
                    if (club.confederation == Confederation.UEFA) uclQualifiers.add(club)
                    if (club.confederation == Confederation.CAF) cafClQualifiers.add(club)
                }
            }

            // Execute Promotion & Relegation between adjacent tiers
            val updatedTierTeams = sortedDivs.associate { it.tierLevel to it.teamIds.toMutableList() }.toMutableMap()

            for (i in 0 until sortedDivs.size - 1) {
                val upperDiv = sortedDivs[i]
                val lowerDiv = sortedDivs[i + 1]
                val upperTable = standingsPerTier[upperDiv.tierLevel] ?: continue
                val lowerTable = standingsPerTier[lowerDiv.tierLevel] ?: continue

                val swapCount = min(upperDiv.relegationSlots, lowerDiv.autoPromotionSlots + lowerDiv.playoffPromotionSlots)
                    .coerceAtMost(min(upperTable.size / 3, lowerTable.size / 3))

                if (swapCount > 0) {
                    val relegatedIds = upperTable.takeLast(swapCount).map { it.teamId }
                    val promotedIds = lowerTable.take(swapCount).map { it.teamId }

                    updatedTierTeams[upperDiv.tierLevel]?.removeAll(relegatedIds)
                    updatedTierTeams[upperDiv.tierLevel]?.addAll(promotedIds)

                    updatedTierTeams[lowerDiv.tierLevel]?.removeAll(promotedIds)
                    updatedTierTeams[lowerDiv.tierLevel]?.addAll(relegatedIds)

                    val promNames = promotedIds.mapNotNull { teamMap[it]?.name }.joinToString(", ")
                    val relNames = relegatedIds.mapNotNull { teamMap[it]?.name }.joinToString(", ")
                    logs.add("$country (${upperDiv.name} ↔ ${lowerDiv.name}): ⬆️ Promoted: $promNames | ⬇️ Relegated: $relNames")
                }
            }

            // Generate next season fixtures for each division
            sortedDivs.forEach { div ->
                val newTeamIds = updatedTierTeams[div.tierLevel] ?: div.teamIds
                val divTeams = newTeamIds.mapNotNull { teamMap[it] }
                val newMatches = SimulationEngine.generateTournamentInitialFixtures(
                    tournamentId = "${div.id}_$nextYear",
                    format = TournamentFormat.DOUBLE_ROUND_ROBIN,
                    teams = divTeams,
                    groupsMap = mapOf(div.name to newTeamIds)
                )
                nextSeasonDivisions.add(
                    div.copy(
                        teamIds = newTeamIds,
                        matches = newMatches
                    )
                )
            }
        }

        // Regenerate Continental Cups with newly qualified teams
        val allClubs = state.allTeams.filter { it.category == TeamCategory.MENS_CLUB }
        val uclPool = (uclQualifiers + allClubs.filter { it.confederation == Confederation.UEFA && it !in uclQualifiers }.sortedByDescending { it.overallRating }).take(16)
        val uclId = "season_ucl_$nextYear"
        val (uclGroups, uclLog) = SimulationEngine.performPotBasedGroupDraw(uclPool, 4, 4, confederationProtection = false)
        val nextUcl = TournamentInstance(
            id = uclId,
            name = "UEFA Champions League $nextYear/${(nextYear + 1) % 100}",
            subtitle = "Continental Qualification • Season Mode",
            yearLabel = "$nextYear-${(nextYear + 1) % 100}",
            hostCountries = listOf("Europe"),
            category = TeamCategory.MENS_CLUB,
            format = TournamentFormat.GROUP_PLUS_KNOCKOUT,
            teamsCount = 16,
            groupsCount = 4,
            teamsPerGroup = 4,
            participantTeamIds = uclPool.map { it.id },
            groupsMap = uclGroups,
            matches = SimulationEngine.generateTournamentInitialFixtures(uclId, TournamentFormat.GROUP_PLUS_KNOCKOUT, uclPool, uclGroups, twoLeggedKnockout = true),
            drawHistoryLog = uclLog
        )

        val cafPool = (cafClQualifiers + allClubs.filter { it.confederation == Confederation.CAF && it !in cafClQualifiers }.sortedByDescending { it.overallRating }).take(8)
        val cafId = "season_caf_$nextYear"
        val (cafGroups, cafLog) = SimulationEngine.performPotBasedGroupDraw(cafPool, 2, 4, confederationProtection = false)
        val nextCaf = TournamentInstance(
            id = cafId,
            name = "CAF Champions League $nextYear/${(nextYear + 1) % 100}",
            subtitle = "African Continental Cup • Season Mode",
            yearLabel = "$nextYear-${(nextYear + 1) % 100}",
            hostCountries = listOf("Africa"),
            category = TeamCategory.MENS_CLUB,
            format = TournamentFormat.GROUP_PLUS_KNOCKOUT,
            teamsCount = 8,
            groupsCount = 2,
            teamsPerGroup = 4,
            participantTeamIds = cafPool.map { it.id },
            groupsMap = cafGroups,
            matches = SimulationEngine.generateTournamentInitialFixtures(cafId, TournamentFormat.GROUP_PLUS_KNOCKOUT, cafPool, cafGroups, twoLeggedKnockout = true),
            drawHistoryLog = cafLog
        )

        _uiState.update {
            it.copy(
                seasonMode = season.copy(
                    seasonYear = nextYear,
                    divisions = nextSeasonDivisions,
                    continentalCups = listOf(nextUcl, nextCaf),
                    seasonHistoryLogs = logs,
                    currentMatchday = 1
                ),
                statusBannerMessage = "Advanced to Season $nextYear/${(nextYear + 1) % 100}! Promotions, relegations & continental qualifiers updated."
            )
        }
        persistStateToDisk()
    }

    // =========================================================================
    // TEAM EDITOR & IMPORT / EXPORT SYSTEM
    // =========================================================================

    fun saveOrUpdateTeam(team: UtsTeam) {
        _uiState.update { state ->
            val exists = state.allTeams.any { it.id == team.id }
            val updatedList = if (exists) {
                state.allTeams.map { if (it.id == team.id) team else it }
            } else {
                listOf(team) + state.allTeams
            }
            state.copy(
                allTeams = updatedList,
                statusBannerMessage = "Saved team '${team.name}' (OVR ${team.overallRating})!"
            )
        }
        persistStateToDisk()
    }

    fun duplicateTeam(team: UtsTeam) {
        val copy = team.copy(
            id = "team_custom_${System.currentTimeMillis()}",
            name = "${team.name} II"
        )
        saveOrUpdateTeam(copy)
    }

    fun deleteTeam(teamId: String) {
        _uiState.update { state ->
            state.copy(
                allTeams = state.allTeams.filterNot { it.id == teamId },
                statusBannerMessage = "Team deleted from database."
            )
        }
        persistStateToDisk()
    }

    fun exportDatabaseAsJson(): String {
        return try {
            if (storageFile.exists()) storageFile.readText() else "{}"
        } catch (_: Exception) {
            "{}"
        }
    }

    fun importDatabaseFromJson(jsonText: String): Boolean {
        return try {
            val root = JSONObject(jsonText)
            val currentTeams = _uiState.value.allTeams
            val importedTeams = parseTeamsJson(root.optJSONArray("customTeams"), currentTeams)
            _uiState.update {
                it.copy(
                    allTeams = importedTeams,
                    statusBannerMessage = "Imported ${importedTeams.size} teams successfully!"
                )
            }
            persistStateToDisk()
            true
        } catch (_: Exception) {
            _uiState.update { it.copy(statusBannerMessage = "Invalid JSON format. Please verify data pack structure.") }
            false
        }
    }

    // =========================================================================
    // COMPLETE 211 FIFA NATIONAL TEAMS + REAL CLUBS + HISTORICAL DATASETS
    // =========================================================================

    private fun buildAllNationalAndClubTeams(): List<UtsTeam> {
        val teams = mutableListOf<UtsTeam>()

        fun addNation(name: String, code: String, flag: String, confed: Confederation, ovr: Int, stadium: String = "$name National Stadium", color: String = "#35D07F") {
            val att = (ovr + (code.hashCode() % 3)).coerceIn(35, 99)
            val mid = (ovr + ((code.hashCode() / 2) % 3)).coerceIn(35, 99)
            val def = (ovr - (code.hashCode() % 2)).coerceIn(35, 99)
            val gk = (ovr - 1).coerceIn(35, 99)
            teams.add(
                UtsTeam(
                    id = "nat_m_${code.lowercase()}",
                    name = name,
                    shortCode = code,
                    country = name,
                    flagEmoji = flag,
                    confederation = confed,
                    category = TeamCategory.MENS_NATIONAL,
                    overallRating = ovr,
                    attackRating = att,
                    midfieldRating = mid,
                    defenseRating = def,
                    goalkeeperRating = gk,
                    stadiumName = stadium,
                    primaryColorHex = color
                )
            )
            // Women's & Youth National Teams for major football nations
            if (ovr >= 74) {
                teams.add(
                    UtsTeam(
                        id = "nat_w_${code.lowercase()}",
                        name = "$name Women",
                        shortCode = code,
                        country = name,
                        flagEmoji = flag,
                        confederation = confed,
                        category = TeamCategory.WOMENS_NATIONAL,
                        overallRating = (ovr - 1).coerceIn(60, 95),
                        attackRating = att,
                        midfieldRating = mid,
                        defenseRating = def,
                        goalkeeperRating = gk,
                        stadiumName = stadium
                    )
                )
                teams.add(
                    UtsTeam(
                        id = "nat_u20_${code.lowercase()}",
                        name = "$name U-20",
                        shortCode = code,
                        country = name,
                        flagEmoji = flag,
                        confederation = confed,
                        category = TeamCategory.U20_MENS_NATIONAL,
                        overallRating = (ovr - 6).coerceIn(55, 90),
                        attackRating = (att - 5).coerceAtLeast(50),
                        midfieldRating = (mid - 5).coerceAtLeast(50),
                        defenseRating = (def - 5).coerceAtLeast(50),
                        goalkeeperRating = (gk - 5).coerceAtLeast(50),
                        stadiumName = stadium
                    )
                )
            }
        }

        // UEFA (55 FIFA Member Associations)
        val uefaData = listOf(
            Triple("Spain", "ESP", "🇪🇸" to 95), Triple("France", "FRA", "🇫🇷" to 94), Triple("England", "ENG", "🏴󠁧󠁢󠁥󠁮󠁧󠁿" to 93),
            Triple("Germany", "GER", "🇩🇪" to 92), Triple("Portugal", "POR", "🇵🇹" to 91), Triple("Netherlands", "NED", "🇳🇱" to 90),
            Triple("Italy", "ITA", "🇮🇹" to 89), Triple("Belgium", "BEL", "🇧🇪" to 87), Triple("Croatia", "CRO", "🇭🇷" to 86),
            Triple("Switzerland", "SUI", "🇨🇭" to 84), Triple("Denmark", "DEN", "🇩🇰" to 83), Triple("Austria", "AUT", "🇦🇹" to 83),
            Triple("Turkey", "TUR", "🇹🇷" to 82), Triple("Ukraine", "UKR", "🇺🇦" to 81), Triple("Serbia", "SRB", "🇷🇸" to 80),
            Triple("Poland", "POL", "🇵🇱" to 79), Triple("Sweden", "SWE", "🇸🇪" to 79), Triple("Czechia", "CZE", "🇨🇿" to 79),
            Triple("Scotland", "SCO", "🏴󠁧󠁢󠁳󠁣󠁴󠁿" to 78), Triple("Hungary", "HUN", "🇭🇺" to 78), Triple("Norway", "NOR", "🇳🇴" to 80),
            Triple("Greece", "GRE", "🇬🇷" to 78), Triple("Slovakia", "SVK", "🇸🇰" to 77), Triple("Romania", "ROU", "🇷🇴" to 77),
            Triple("Wales", "WAL", "🏴󠁧󠁢󠁷󠁬󠁳󠁿" to 76), Triple("Slovenia", "SVN", "🇸🇮" to 76), Triple("Georgia", "GEO", "🇬🇪" to 76),
            Triple("Russia", "RUS", "🇷🇺" to 77), Triple("Republic of Ireland", "IRL", "🇮🇪" to 74), Triple("Finland", "FIN", "🇫🇮" to 73),
            Triple("Albania", "ALB", "🇦🇱" to 74), Triple("North Macedonia", "MKD", "🇲🇰" to 72), Triple("Bosnia and Herzegovina", "BIH", "🇧🇦" to 73),
            Triple("Iceland", "ISL", "🇮🇸" to 72), Triple("Northern Ireland", "NIR", "🇬🇧" to 71), Triple("Montenegro", "MNE", "🇲🇪" to 71),
            Triple("Israel", "ISR", "🇮🇱" to 72), Triple("Bulgaria", "BUL", "🇧🇬" to 70), Triple("Luxembourg", "LUX", "🇱🇺" to 69),
            Triple("Armenia", "ARM", "🇦🇲" to 68), Triple("Belarus", "BLR", "🇧🇾" to 67), Triple("Kosovo", "KOS", "🇽🇰" to 70),
            Triple("Kazakhstan", "KAZ", "🇰🇿" to 67), Triple("Azerbaijan", "AZE", "🇦🇿" to 67), Triple("Cyprus", "CYP", "🇨🇾" to 65),
            Triple("Estonia", "EST", "🇪🇪" to 64), Triple("Latvia", "LVA", "🇱🇻" to 63), Triple("Lithuania", "LTU", "🇱🇹" to 63),
            Triple("Faroe Islands", "FRO", "🇫🇴" to 62), Triple("Moldova", "MDA", "🇲🇩" to 63), Triple("Malta", "MLT", "🇲🇹" to 60),
            Triple("Andorra", "AND", "🇦🇩" to 55), Triple("Gibraltar", "GIB", "🇬🇮" to 54), Triple("Liechtenstein", "LIE", "🇱🇮" to 53),
            Triple("San Marino", "SMR", "🇸🇲" to 48)
        )
        uefaData.forEach { (name, code, pair) -> addNation(name, code, pair.first, Confederation.UEFA, pair.second) }

        // CONMEBOL (10 FIFA Member Associations)
        val conmebolData = listOf(
            Triple("Argentina", "ARG", "🇦🇷" to 96), Triple("Brazil", "BRA", "🇧🇷" to 93), Triple("Uruguay", "URU", "🇺🇾" to 89),
            Triple("Colombia", "COL", "🇨🇴" to 88), Triple("Ecuador", "ECU", "🇪🇨" to 83), Triple("Venezuela", "VEN", "🇻🇪" to 79),
            Triple("Chile", "CHI", "🇨🇱" to 78), Triple("Paraguay", "PAR", "🇵🇾" to 78), Triple("Peru", "PER", "🇵🇪" to 77),
            Triple("Bolivia", "BOL", "🇧🇴" to 73)
        )
        conmebolData.forEach { (name, code, pair) -> addNation(name, code, pair.first, Confederation.CONMEBOL, pair.second) }

        // CAF (54 FIFA Member Associations)
        val cafData = listOf(
            Triple("Morocco", "MAR", "🇲🇦" to 87), Triple("Senegal", "SEN", "🇸🇳" to 85), Triple("Algeria", "ALG", "🇩🇿" to 84),
            Triple("Egypt", "EGY", "🇪🇬" to 83), Triple("Nigeria", "NGA", "🇳🇬" to 84), Triple("Ivory Coast", "CIV", "🇨🇮" to 84),
            Triple("Cameroon", "CMR", "🇨🇲" to 81), Triple("Tunisia", "TUN", "🇹🇳" to 80), Triple("Mali", "MLI", "🇲🇱" to 80),
            Triple("South Africa", "RSA", "🇿🇦" to 79), Triple("DR Congo", "COD", "🇨🇩" to 79), Triple("Ghana", "GHA", "🇬🇭" to 78),
            Triple("Burkina Faso", "BFA", "🇧🇫" to 77), Triple("Guinea", "GUI", "🇬🇳" to 76), Triple("Cape Verde", "CPV", "🇨🇻" to 76),
            Triple("Angola", "ANG", "🇦🇴" to 76), Triple("Equatorial Guinea", "EQG", "🇬🇶" to 74), Triple("Gabon", "GAB", "🇬🇦" to 74),
            Triple("Zambia", "ZAM", "🇿🇲" to 73), Triple("Uganda", "UGA", "🇺🇬" to 71), Triple("Benin", "BEN", "🇧🇯" to 71),
            Triple("Mauritania", "MTN", "🇲🇷" to 71), Triple("Mozambique", "MOZ", "🇲🇿" to 71), Triple("Namibia", "NAM", "🇳🇦" to 70),
            Triple("Madagascar", "MAD", "🇲🇬" to 69), Triple("Kenya", "KEN", "🇰🇪" to 69), Triple("Togo", "TOG", "🇹🇬" to 69),
            Triple("Guinea-Bissau", "GNB", "🇬🇼" to 69), Triple("Libya", "LBY", "🇱🇾" to 69), Triple("Tanzania", "TAN", "🇹🇿" to 69),
            Triple("Comoros", "COM", "🇰🇲" to 70), Triple("Zimbabwe", "ZIM", "🇿🇼" to 69), Triple("Sudan", "SDN", "🇸🇩" to 69),
            Triple("Malawi", "MWI", "🇲🇼" to 67), Triple("Sierra Leone", "SLE", "🇸🇱" to 67), Triple("Congo", "CGO", "🇨🇬" to 67),
            Triple("Central African Republic", "CTA", "🇨🇫" to 66), Triple("Niger", "NIG", "🇳🇪" to 66), Triple("Rwanda", "RWA", "🇷🇼" to 67),
            Triple("Gambia", "GAM", "🇬🇲" to 70), Triple("Burundi", "BDI", "🇧🇮" to 65), Triple("Ethiopia", "ETH", "🇪🇹" to 64),
            Triple("Botswana", "BOT", "🇧🇼" to 65), Triple("Liberia", "LBR", "🇱🇷" to 63), Triple("Lesotho", "LES", "🇱🇸" to 62),
            Triple("Eswatini", "SWZ", "🇸🇿" to 61), Triple("South Sudan", "SSD", "🇸🇸" to 61), Triple("Mauritius", "MRI", "🇲🇺" to 59),
            Triple("Chad", "CHA", "🇹🇩" to 59), Triple("Sao Tome and Principe", "STP", "🇸🇹" to 57), Triple("Djibouti", "DJI", "🇩🇯" to 56),
            Triple("Somalia", "SOM", "🇸🇴" to 54), Triple("Seychelles", "SEY", "🇸🇨" to 53), Triple("Eritrea", "ERI", "🇪🇷" to 55)
        )
        cafData.forEach { (name, code, pair) -> addNation(name, code, pair.first, Confederation.CAF, pair.second) }

        // AFC (47 FIFA Member Associations)
        val afcData = listOf(
            Triple("Japan", "JPN", "🇯🇵" to 86), Triple("South Korea", "KOR", "🇰🇷" to 84), Triple("Iran", "IRN", "🇮🇷" to 83),
            Triple("Australia", "AUS", "🇦🇺" to 82), Triple("Saudi Arabia", "KSA", "🇸🇦" to 79), Triple("Qatar", "QAT", "🇶🇦" to 79),
            Triple("Iraq", "IRQ", "🇮🇶" to 78), Triple("Uzbekistan", "UZB", "🇺🇿" to 78), Triple("Jordan", "JOR", "🇯🇴" to 77),
            Triple("UAE", "UAE", "🇦🇪" to 76), Triple("Oman", "OMA", "🇴🇲" to 74), Triple("Bahrain", "BHR", "🇧🇭" to 74),
            Triple("China", "CHN", "🇨🇳" to 72), Triple("Syria", "SYR", "🇸🇾" to 72), Triple("Palestine", "PLE", "🇵🇸" to 73),
            Triple("Thailand", "THA", "🇹🇭" to 71), Triple("Kyrgyzstan", "KGZ", "🇰🇬" to 70), Triple("Vietnam", "VIE", "🇻🇳" to 70),
            Triple("Lebanon", "LBN", "🇱🇧" to 69), Triple("North Korea", "PRK", "🇰🇵" to 70), Triple("Tajikistan", "TJK", "🇹🇯" to 70),
            Triple("India", "IND", "🇮🇳" to 68), Triple("Indonesia", "IDN", "🇮🇩" to 71), Triple("Kuwait", "KUW", "🇰🇼" to 68),
            Triple("Malaysia", "MAS", "🇲🇾" to 68), Triple("Turkmenistan", "TKM", "🇹🇲" to 66), Triple("Philippines", "PHI", "🇵🇭" to 65),
            Triple("Afghanistan", "AFG", "🇦🇫" to 63), Triple("Hong Kong", "HKG", "🇭🇰" to 64), Triple("Yemen", "YEM", "🇾🇪" to 63),
            Triple("Singapore", "SGP", "🇸🇬" to 63), Triple("Myanmar", "MYA", "🇲🇲" to 61), Triple("Chinese Taipei", "TPE", "🇹🇼" to 60),
            Triple("Maldives", "MDV", "🇲🇻" to 59), Triple("Nepal", "NEP", "🇳🇵" to 58), Triple("Cambodia", "CAM", "🇰🇭" to 58),
            Triple("Bangladesh", "BAN", "🇧🇩" to 57), Triple("Bhutan", "BHU", "🇧🇹" to 55), Triple("Laos", "LAO", "🇱🇦" to 55),
            Triple("Brunei", "BRU", "🇧🇳" to 54), Triple("Macau", "MAC", "🇲🇴" to 53), Triple("Mongolia", "MNG", "🇲🇳" to 54),
            Triple("Pakistan", "PAK", "🇵🇰" to 56), Triple("Sri Lanka", "SRI", "🇱🇰" to 54), Triple("Timor-Leste", "TLS", "🇹🇱" to 52),
            Triple("Guam", "GUM", "🇬🇺" to 51)
        )
        afcData.forEach { (name, code, pair) -> addNation(name, code, pair.first, Confederation.AFC, pair.second) }

        // CONCACAF (35 FIFA Member Associations)
        val concacafData = listOf(
            Triple("USA", "USA", "🇺🇸" to 84), Triple("Mexico", "MEX", "🇲🇽" to 83), Triple("Canada", "CAN", "🇨🇦" to 82),
            Triple("Panama", "PAN", "🇵🇦" to 78), Triple("Costa Rica", "CRC", "🇨🇷" to 77), Triple("Jamaica", "JAM", "🇯🇲" to 76),
            Triple("Honduras", "HON", "🇭🇳" to 74), Triple("El Salvador", "SLV", "🇸🇻" to 71), Triple("Haiti", "HAI", "🇭🇹" to 71),
            Triple("Curacao", "CUW", "🇨🇼" to 70), Triple("Trinidad and Tobago", "TRI", "🇹🇹" to 70), Triple("Guatemala", "GUA", "🇬🇹" to 71),
            Triple("Suriname", "SUR", "🇸🇷" to 69), Triple("Nicaragua", "NCA", "🇳🇮" to 67), Triple("Cuba", "CUB", "🇨🇺" to 65),
            Triple("Dominican Republic", "DOM", "🇩🇴" to 66), Triple("Guyana", "GUY", "🇬🇾" to 64), Triple("Puerto Rico", "PUR", "🇵🇷" to 63),
            Triple("Antigua and Barbuda", "ATG", "🇦🇬" to 62), Triple("St Kitts and Nevis", "SKN", "🇰🇳" to 61),
            Triple("Saint Lucia", "LCA", "🇱🇨" to 61), Triple("Grenada", "GRN", "🇬🇩" to 61), Triple("Bermuda", "BER", "🇧🇲" to 60),
            Triple("Barbados", "BRB", "🇧🇧" to 59), Triple("Belize", "BLZ", "🇧🇿" to 58), Triple("Dominica", "DMA", "🇩🇲" to 57)
        )
        concacafData.forEach { (name, code, pair) -> addNation(name, code, pair.first, Confederation.CONCACAF, pair.second) }

        // OFC (11 FIFA Member Associations)
        val ofcData = listOf(
            Triple("New Zealand", "NZL", "🇳🇿" to 75), Triple("Fiji", "FIJ", "🇫🇯" to 64), Triple("Solomon Islands", "SOL", "🇸🇧" to 64),
            Triple("New Caledonia", "NCL", "🇳🇨" to 64), Triple("Tahiti", "TAH", "🇵🇫" to 63), Triple("Vanuatu", "VAN", "🇻🇺" to 62),
            Triple("Papua New Guinea", "PNG", "🇵🇬" to 62), Triple("Samoa", "SAM", "🇼🇸" to 56), Triple("Tonga", "TGA", "🇹🇴" to 53),
            Triple("Cook Islands", "COK", "🇨🇰" to 53), Triple("American Samoa", "ASA", "🇦🇸" to 50)
        )
        ofcData.forEach { (name, code, pair) -> addNation(name, code, pair.first, Confederation.OFC, pair.second) }

        // =====================================================================
        // REAL MEN'S CLUB FOOTBALL DATABASE (MULTI-DIVISION & CONTINENTAL)
        // =====================================================================
        fun addClub(name: String, code: String, country: String, flag: String, confed: Confederation, ovr: Int, tier: Int = 1, stadium: String = "$name Arena") {
            teams.add(
                UtsTeam(
                    id = "club_${country.lowercase().replace(" ", "")}_${code.lowercase()}_${teams.size}",
                    name = name,
                    shortCode = code,
                    country = country,
                    flagEmoji = flag,
                    confederation = confed,
                    category = TeamCategory.MENS_CLUB,
                    overallRating = ovr,
                    attackRating = (ovr + 1).coerceIn(40, 99),
                    midfieldRating = ovr.coerceIn(40, 99),
                    defenseRating = (ovr - 1).coerceIn(40, 99),
                    goalkeeperRating = ovr.coerceIn(40, 99),
                    stadiumName = stadium,
                    domesticTier = tier
                )
            )
        }

        // England Tiers 1-5 (Premier League, Championship, League One, League Two, National League)
        val englandT1 = listOf(
            "Manchester City" to 95, "Arsenal" to 92, "Liverpool" to 93, "Chelsea" to 88, "Aston Villa" to 86,
            "Tottenham Hotspur" to 86, "Newcastle United" to 86, "Manchester United" to 85, "Brighton" to 83,
            "West Ham United" to 82, "Crystal Palace" to 81, "Fulham" to 81, "Bournemouth" to 80, "Wolverhampton" to 79,
            "Everton" to 79, "Brentford" to 80, "Nottingham Forest" to 80, "Leicester City" to 78, "Ipswich Town" to 77, "Southampton" to 77
        )
        englandT1.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", Confederation.UEFA, r, 1) }

        val englandT2 = listOf(
            "Leeds United" to 78, "Burnley" to 77, "Sheffield United" to 77, "Middlesbrough" to 75, "West Bromwich Albion" to 75,
            "Sunderland" to 75, "Norwich City" to 74, "Coventry City" to 74, "Watford" to 73, "Blackburn Rovers" to 72,
            "Swansea City" to 72, "Bristol City" to 72
        )
        englandT2.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", Confederation.UEFA, r, 2) }

        val englandT3 = listOf(
            "Birmingham City" to 73, "Wrexham" to 71, "Bolton Wanderers" to 70, "Huddersfield Town" to 70,
            "Charlton Athletic" to 69, "Reading" to 68, "Wigan Athletic" to 68, "Barnsley" to 68
        )
        englandT3.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", Confederation.UEFA, r, 3) }

        val englandT4 = listOf(
            "MK Dons" to 65, "Bradford City" to 64, "Notts County" to 64, "Port Vale" to 64,
            "AFC Wimbledon" to 63, "Carlisle United" to 63, "Chesterfield" to 64, "Gillingham" to 63
        )
        englandT4.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", Confederation.UEFA, r, 4) }

        val englandT5 = listOf(
            "Oldham Athletic" to 60, "Southend United" to 59, "Barnet" to 60, "York City" to 59,
            "Hartlepool United" to 58, "Rochdale" to 58, "Forest Green Rovers" to 59, "Dagenham & Redbridge" to 57
        )
        englandT5.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", Confederation.UEFA, r, 5) }

        // Spain (La Liga & Segunda División)
        val spainT1 = listOf(
            "Real Madrid" to 96, "FC Barcelona" to 93, "Atlético Madrid" to 89, "Athletic Club" to 85, "Real Sociedad" to 84,
            "Villarreal" to 83, "Real Betis" to 82, "Girona FC" to 82, "Sevilla FC" to 80, "Valencia CF" to 79,
            "Celta Vigo" to 79, "Osasuna" to 79
        )
        spainT1.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "Spain", "🇪🇸", Confederation.UEFA, r, 1) }

        val spainT2 = listOf(
            "Racing Santander" to 75, "Real Zaragoza" to 74, "Sporting Gijón" to 74, "UD Almería" to 75,
            "Granada CF" to 74, "Cádiz CF" to 74, "Deportivo La Coruña" to 74, "Málaga CF" to 73
        )
        spainT2.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "Spain", "🇪🇸", Confederation.UEFA, r, 2) }

        // Germany (Bundesliga & 2. Bundesliga)
        val germanyT1 = listOf(
            "Bayern Munich" to 94, "Bayer Leverkusen" to 91, "Borussia Dortmund" to 89, "RB Leipzig" to 87,
            "VfB Stuttgart" to 84, "Eintracht Frankfurt" to 83, "SC Freiburg" to 81, "VfL Wolfsburg" to 80,
            "Borussia Mönchengladbach" to 79, "Werder Bremen" to 79
        )
        germanyT1.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "Germany", "🇩🇪", Confederation.UEFA, r, 1) }

        val germanyT2 = listOf(
            "Hamburger SV" to 76, "FC Köln" to 76, "Schalke 04" to 74, "Hertha BSC" to 75,
            "Fortuna Düsseldorf" to 75, "Hannover 96" to 74, "FC Kaiserslautern" to 73, "FC Nürnberg" to 73
        )
        germanyT2.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "Germany", "🇩🇪", Confederation.UEFA, r, 2) }

        // Italy (Serie A & Serie B)
        val italyT1 = listOf(
            "Inter Milan" to 92, "AC Milan" to 88, "Juventus" to 89, "Atalanta" to 87, "Napoli" to 88,
            "AS Roma" to 85, "Lazio" to 84, "Fiorentina" to 83, "Bologna" to 82, "Torino" to 79
        )
        italyT1.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "Italy", "🇮🇹", Confederation.UEFA, r, 1) }

        val italyT2 = listOf(
            "Sassuolo" to 76, "Palermo" to 74, "Sampdoria" to 74, "Cremonese" to 74,
            "Pisa" to 74, "Spezia" to 73, "Bari" to 72, "Salernitana" to 73
        )
        italyT2.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "Italy", "🇮🇹", Confederation.UEFA, r, 2) }

        // France (Ligue 1 & Ligue 2)
        val franceT1 = listOf(
            "Paris Saint-Germain" to 92, "AS Monaco" to 85, "Olympique Marseille" to 85, "LOSC Lille" to 84,
            "Olympique Lyonnais" to 83, "OGC Nice" to 82, "RC Lens" to 81, "Stade Rennais" to 80
        )
        franceT1.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "France", "🇫🇷", Confederation.UEFA, r, 1) }

        val franceT2 = listOf(
            "FC Lorient" to 75, "FC Metz" to 75, "Paris FC" to 75, "SM Caen" to 72,
            "EA Guingamp" to 72, "SC Bastia" to 71, "Amiens SC" to 71, "AC Ajaccio" to 70
        )
        franceT2.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "France", "🇫🇷", Confederation.UEFA, r, 2) }

        // Algeria (Ligue 1 Mobilis & Ligue 2)
        val algeriaT1 = listOf(
            "MC Alger" to 80, "CR Belouizdad" to 80, "CS Constantine" to 79, "USM Alger" to 79,
            "JS Kabylie" to 78, "ES Sétif" to 78, "MC Oran" to 76, "Paradou AC" to 76,
            "JS Saoura" to 75, "ASO Chlef" to 75, "USM Khenchela" to 74, "NC Magra" to 73,
            "MC El Bayadh" to 73, "ES Mostaganem" to 73, "Olympique Akbou" to 74, "US Biskra" to 73
        )
        algeriaT1.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "Algeria", "🇩🇿", Confederation.CAF, r, 1) }

        val algeriaT2 = listOf(
            "NA Hussein Dey" to 70, "RC Kouba" to 70, "USM El Harrach" to 71, "WA Tlemcen" to 69,
            "JSM Béjaïa" to 69, "CA Batna" to 69, "USM Annaba" to 70, "ASM Oran" to 69
        )
        algeriaT2.forEach { (n, r) -> addClub(n, n.take(3).uppercase(), "Algeria", "🇩🇿", Confederation.CAF, r, 2) }

        // Other Major Global Clubs (UEFA, CONMEBOL, CAF, AFC, CONCACAF, OFC)
        val globalClubs = listOf(
            Triple("Sporting CP", "Portugal" to "🇵🇹", Confederation.UEFA to 85),
            Triple("SL Benfica", "Portugal" to "🇵🇹", Confederation.UEFA to 85),
            Triple("FC Porto", "Portugal" to "🇵🇹", Confederation.UEFA to 84),
            Triple("PSV Eindhoven", "Netherlands" to "🇳🇱", Confederation.UEFA to 84),
            Triple("Feyenoord", "Netherlands" to "🇳🇱", Confederation.UEFA to 82),
            Triple("Ajax", "Netherlands" to "🇳🇱", Confederation.UEFA to 81),
            Triple("Club Brugge", "Belgium" to "🇧🇪", Confederation.UEFA to 80),
            Triple("Celtic FC", "Scotland" to "🏴󠁧󠁢󠁳󠁣󠁴󠁿", Confederation.UEFA to 80),
            Triple("Rangers FC", "Scotland" to "🏴󠁧󠁢󠁳󠁣󠁴󠁿", Confederation.UEFA to 78),
            Triple("Galatasaray", "Turkey" to "🇹🇷", Confederation.UEFA to 83),
            Triple("Fenerbahçe", "Turkey" to "🇹🇷", Confederation.UEFA to 82),
            Triple("Olympiacos", "Greece" to "🇬🇷", Confederation.UEFA to 80),
            Triple("Flamengo", "Brazil" to "🇧🇷", Confederation.CONMEBOL to 85),
            Triple("Palmeiras", "Brazil" to "🇧🇷", Confederation.CONMEBOL to 85),
            Triple("Botafogo", "Brazil" to "🇧🇷", Confederation.CONMEBOL to 84),
            Triple("Atlético Mineiro", "Brazil" to "🇧🇷", Confederation.CONMEBOL to 82),
            Triple("São Paulo FC", "Brazil" to "🇧🇷", Confederation.CONMEBOL to 81),
            Triple("River Plate", "Argentina" to "🇦🇷", Confederation.CONMEBOL to 84),
            Triple("Boca Juniors", "Argentina" to "🇦🇷", Confederation.CONMEBOL to 83),
            Triple("Racing Club", "Argentina" to "🇦🇷", Confederation.CONMEBOL to 82),
            Triple("Deportes Tolima", "Colombia" to "🇨🇴", Confederation.CONMEBOL to 77),
            Triple("Atlético Nacional", "Colombia" to "🇨🇴", Confederation.CONMEBOL to 78),
            Triple("Al Ahly SC", "Egypt" to "🇪🇬", Confederation.CAF to 84),
            Triple("Zamalek SC", "Egypt" to "🇪🇬", Confederation.CAF to 81),
            Triple("Pyramids FC", "Egypt" to "🇪🇬", Confederation.CAF to 80),
            Triple("Wydad AC", "Morocco" to "🇲🇦", Confederation.CAF to 81),
            Triple("Raja CA", "Morocco" to "🇲🇦", Confederation.CAF to 81),
            Triple("RS Berkane", "Morocco" to "🇲🇦", Confederation.CAF to 80),
            Triple("Espérance de Tunis", "Tunisia" to "🇹🇳", Confederation.CAF to 82),
            Triple("Mamelodi Sundowns", "South Africa" to "🇿🇦", Confederation.CAF to 82),
            Triple("TP Mazembe", "DR Congo" to "🇨🇩", Confederation.CAF to 78),
            Triple("Al-Hilal SFC", "Saudi Arabia" to "🇸🇦", Confederation.AFC to 86),
            Triple("Al-Nassr FC", "Saudi Arabia" to "🇸🇦", Confederation.AFC to 84),
            Triple("Al-Ittihad", "Saudi Arabia" to "🇸🇦", Confederation.AFC to 83),
            Triple("Al-Ahli Saudi", "Saudi Arabia" to "🇸🇦", Confederation.AFC to 83),
            Triple("Al Ain FC", "UAE" to "🇦🇪", Confederation.AFC to 80),
            Triple("Urawa Red Diamonds", "Japan" to "🇯🇵", Confederation.AFC to 79),
            Triple("Yokohama F. Marinos", "Japan" to "🇯🇵", Confederation.AFC to 79),
            Triple("Ulsan HD", "South Korea" to "🇰🇷", Confederation.AFC to 79),
            Triple("Club América", "Mexico" to "🇲🇽", Confederation.CONCACAF to 82),
            Triple("CF Monterrey", "Mexico" to "🇲🇽", Confederation.CONCACAF to 81),
            Triple("Tigres UANL", "Mexico" to "🇲🇽", Confederation.CONCACAF to 81),
            Triple("CF Pachuca", "Mexico" to "🇲🇽", Confederation.CONCACAF to 80),
            Triple("Inter Miami CF", "USA" to "🇺🇸", Confederation.CONCACAF to 81),
            Triple("Columbus Crew", "USA" to "🇺🇸", Confederation.CONCACAF to 80),
            Triple("LA Galaxy", "USA" to "🇺🇸", Confederation.CONCACAF to 79),
            Triple("Auckland City FC", "New Zealand" to "🇳🇿", Confederation.OFC to 71)
        )
        globalClubs.forEach { (name, countryPair, confPair) ->
            addClub(name, name.take(3).uppercase(), countryPair.first, countryPair.second, confPair.first, confPair.second, 1)
        }

        return teams
    }

    private fun buildVerifiedWorldCupHistory(): List<HistoricalWorldCupEdition> {
        return listOf(
            // MEN'S FIFA WORLD CUPS (1930 - 2026)
            HistoricalWorldCupEdition(2026, TeamCategory.MENS_NATIONAL, "USA / Canada / Mexico", "🇺🇸🇨🇦🇲🇽", 48, "TBD (2026 Edition)", "🏆", "TBD", "🥈", "TBD", "🥉", "TBD", "48-Team Expanded Format (104 Matches)", "MetLife Stadium, New York/New Jersey", "Active Tournament", 0, 104, DataSourceType.VERIFIED_CURRENT, listOf("12 Groups of 4 teams; Top 2 + 8 Best Third-Placed advance to Round of 32")),
            HistoricalWorldCupEdition(2022, TeamCategory.MENS_NATIONAL, "Qatar", "🇶🇦", 32, "Argentina", "🇦🇷", "France", "🇫🇷", "Croatia", "🇭🇷", "Morocco", "Argentina 3 - 3 France (4-2 pens)", "Lusail Stadium, Lusail", "Kylian Mbappé (8 goals)", 172, 64, DataSourceType.VERIFIED_HISTORICAL, listOf("Final: Argentina 3-3 France (4-2 pens)", "Third Place: Croatia 2-1 Morocco", "SF: Argentina 3-0 Croatia, France 2-0 Morocco")),
            HistoricalWorldCupEdition(2018, TeamCategory.MENS_NATIONAL, "Russia", "🇷🇺", 32, "France", "🇫🇷", "Croatia", "🇭🇷", "Belgium", "🇧🇪", "England", "France 4 - 2 Croatia", "Luzhniki Stadium, Moscow", "Harry Kane (6 goals)", 169, 64, DataSourceType.VERIFIED_HISTORICAL, listOf("Final: France 4-2 Croatia", "Third Place: Belgium 2-0 England", "R16: France 4-3 Argentina")),
            HistoricalWorldCupEdition(2014, TeamCategory.MENS_NATIONAL, "Brazil", "🇧🇷", 32, "Germany", "🇩🇪", "Argentina", "🇦🇷", "Netherlands", "🇳🇱", "Brazil", "Germany 1 - 0 Argentina (AET)", "Maracanã, Rio de Janeiro", "James Rodríguez (6 goals)", 171, 64, DataSourceType.VERIFIED_HISTORICAL, listOf("Final: Germany 1-0 Argentina (Götze 113')", "SF: Germany 7-1 Brazil", "Group B: Netherlands 5-1 Spain")),
            HistoricalWorldCupEdition(2010, TeamCategory.MENS_NATIONAL, "South Africa", "🇿🇦", 32, "Spain", "🇪🇸", "Netherlands", "🇳🇱", "Germany", "🇩🇪", "Uruguay", "Spain 1 - 0 Netherlands (AET)", "Soccer City, Johannesburg", "Thomas Müller / David Villa / Wesley Sneijder / Diego Forlán (5)", 145, 64, DataSourceType.VERIFIED_HISTORICAL, listOf("Final: Spain 1-0 Netherlands (Iniesta 116')", "QF: Uruguay 1-1 Ghana (4-2 pens)")),
            HistoricalWorldCupEdition(2006, TeamCategory.MENS_NATIONAL, "Germany", "🇩🇪", 32, "Italy", "🇮🇹", "France", "🇫🇷", "Germany", "🇩🇪", "Portugal", "Italy 1 - 1 France (5-3 pens)", "Olympiastadion, Berlin", "Miroslav Klose (5 goals)", 147, 64, DataSourceType.VERIFIED_HISTORICAL, listOf("Final: Italy 1-1 France (5-3 pens)", "SF: Italy 2-0 Germany (AET)")),
            HistoricalWorldCupEdition(2002, TeamCategory.MENS_NATIONAL, "South Korea / Japan", "🇰🇷🇯🇵", 32, "Brazil", "🇧🇷", "Germany", "🇩🇪", "Turkey", "🇹🇷", "South Korea", "Brazil 2 - 0 Germany", "International Stadium, Yokohama", "Ronaldo (8 goals)", 161, 64, DataSourceType.VERIFIED_HISTORICAL, listOf("Final: Brazil 2-0 Germany (Ronaldo brace)", "Opening Match: Senegal 1-0 France")),
            HistoricalWorldCupEdition(1998, TeamCategory.MENS_NATIONAL, "France", "🇫🇷", 32, "France", "🇫🇷", "Brazil", "🇧🇷", "Croatia", "🇭🇷", "Netherlands", "France 3 - 0 Brazil", "Stade de France, Saint-Denis", "Davor Šuker (6 goals)", 171, 64, DataSourceType.VERIFIED_HISTORICAL, listOf("Final: France 3-0 Brazil (Zidane 2, Petit 1)", "QF: Croatia 3-0 Germany")),
            HistoricalWorldCupEdition(1994, TeamCategory.MENS_NATIONAL, "United States", "🇺🇸", 24, "Brazil", "🇧🇷", "Italy", "🇮🇹", "Sweden", "🇸🇪", "Bulgaria", "Brazil 0 - 0 Italy (3-2 pens)", "Rose Bowl, Pasadena", "Hristo Stoichkov / Oleg Salenko (6)", 141, 52, DataSourceType.VERIFIED_HISTORICAL, listOf("Final: Brazil 0-0 Italy (3-2 pens)", "R16: Romania 3-2 Argentina")),
            HistoricalWorldCupEdition(1990, TeamCategory.MENS_NATIONAL, "Italy", "🇮🇹", 24, "West Germany", "🇩🇪", "Argentina", "🇦🇷", "Italy", "🇮🇹", "England", "West Germany 1 - 0 Argentina", "Stadio Olimpico, Rome", "Salvatore Schillaci (6 goals)", 115, 52, DataSourceType.VERIFIED_HISTORICAL, listOf("Final: West Germany 1-0 Argentina (Brehme 85' pen)", "Opening: Cameroon 1-0 Argentina")),
            HistoricalWorldCupEdition(1986, TeamCategory.MENS_NATIONAL, "Mexico", "🇲🇽", 24, "Argentina", "🇦🇷", "West Germany", "🇩🇪", "France", "🇫🇷", "Belgium", "Argentina 3 - 2 West Germany", "Estadio Azteca, Mexico City", "Gary Lineker (6 goals)", 132, 52, DataSourceType.VERIFIED_HISTORICAL, listOf("Final: Argentina 3-2 West Germany", "QF: Argentina 2-1 England (Maradona)", "Group: Morocco 3-1 Portugal")),
            HistoricalWorldCupEdition(1982, TeamCategory.MENS_NATIONAL, "Spain", "🇪🇸", 24, "Italy", "🇮🇹", "West Germany", "🇩🇪", "Poland", "🇵🇱", "France", "Italy 3 - 1 West Germany", "Santiago Bernabéu, Madrid", "Paolo Rossi (6 goals)", 146, 52, DataSourceType.VERIFIED_HISTORICAL, listOf("Final: Italy 3-1 West Germany", "Historic Group Stage: Algeria 2-1 West Germany (Madjer & Belloumi)", "Second Round: Italy 3-2 Brazil")),
            HistoricalWorldCupEdition(1978, TeamCategory.MENS_NATIONAL, "Argentina", "🇦🇷", 16, "Argentina", "🇦🇷", "Netherlands", "🇳🇱", "Brazil", "🇧🇷", "Italy", "Argentina 3 - 1 Netherlands (AET)", "Estadio Monumental, Buenos Aires", "Mario Kempes (6 goals)", 102, 38, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(1974, TeamCategory.MENS_NATIONAL, "West Germany", "🇩🇪", 16, "West Germany", "🇩🇪", "Netherlands", "🇳🇱", "Poland", "🇵🇱", "Brazil", "West Germany 2 - 1 Netherlands", "Olympiastadion, Munich", "Grzegorz Lato (7 goals)", 97, 38, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(1970, TeamCategory.MENS_NATIONAL, "Mexico", "🇲🇽", 16, "Brazil", "🇧🇷", "Italy", "🇮🇹", "West Germany", "🇩🇪", "Uruguay", "Brazil 4 - 1 Italy", "Estadio Azteca, Mexico City", "Gerd Müller (10 goals)", 95, 32, DataSourceType.VERIFIED_HISTORICAL, listOf("Final: Brazil 4-1 Italy", "SF: Italy 4-3 West Germany (Game of the Century)")),
            HistoricalWorldCupEdition(1966, TeamCategory.MENS_NATIONAL, "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", 16, "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "West Germany", "🇩🇪", "Portugal", "🇵🇹", "Soviet Union", "England 4 - 2 West Germany (AET)", "Wembley Stadium, London", "Eusébio (9 goals)", 89, 32, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(1962, TeamCategory.MENS_NATIONAL, "Chile", "🇨🇱", 16, "Brazil", "🇧🇷", "Czechoslovakia", "🇨🇿", "Chile", "🇨🇱", "Yugoslavia", "Brazil 3 - 1 Czechoslovakia", "Estadio Nacional, Santiago", "Garrincha / Vavá / Leonel Sánchez (4)", 89, 32, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(1958, TeamCategory.MENS_NATIONAL, "Sweden", "🇸🇪", 16, "Brazil", "🇧🇷", "Sweden", "🇸🇪", "France", "🇫🇷", "West Germany", "Brazil 5 - 2 Sweden", "Råsunda Stadium, Solna", "Just Fontaine (13 goals)", 126, 35, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(1954, TeamCategory.MENS_NATIONAL, "Switzerland", "🇨🇭", 16, "West Germany", "🇩🇪", "Hungary", "🇭🇺", "Austria", "🇦🇹", "Uruguay", "West Germany 3 - 2 Hungary", "Wankdorf Stadium, Bern", "Sándor Kocsis (11 goals)", 140, 26, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(1950, TeamCategory.MENS_NATIONAL, "Brazil", "🇧🇷", 13, "Uruguay", "🇺🇾", "Brazil", "🇧🇷", "Sweden", "🇸🇪", "Spain", "Uruguay 2 - 1 Brazil (Final Group Decider)", "Maracanã, Rio de Janeiro", "Ademir (8 goals)", 88, 22, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(1938, TeamCategory.MENS_NATIONAL, "France", "🇫🇷", 15, "Italy", "🇮🇹", "Hungary", "🇭🇺", "Brazil", "🇧🇷", "Sweden", "Italy 4 - 2 Hungary", "Stade Olympique de Colombes, Paris", "Leônidas (7 goals)", 84, 18, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(1934, TeamCategory.MENS_NATIONAL, "Italy", "🇮🇹", 16, "Italy", "🇮🇹", "Czechoslovakia", "🇨🇿", "Germany", "🇩🇪", "Austria", "Italy 2 - 1 Czechoslovakia (AET)", "Stadio Nazionale PNF, Rome", "Oldřich Nejedlý (5 goals)", 70, 17, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(1930, TeamCategory.MENS_NATIONAL, "Uruguay", "🇺🇾", 13, "Uruguay", "🇺🇾", "Argentina", "🇦🇷", "USA", "🇺🇸", "Yugoslavia", "Uruguay 4 - 2 Argentina", "Estadio Centenario, Montevideo", "Guillermo Stábile (8 goals)", 70, 18, DataSourceType.VERIFIED_HISTORICAL),

            // WOMEN'S FIFA WORLD CUPS (1991 - 2027)
            HistoricalWorldCupEdition(2027, TeamCategory.WOMENS_NATIONAL, "Brazil", "🇧🇷", 32, "TBD (2027 Edition)", "🏆", "TBD", "🥈", "TBD", "🥉", "TBD", "32-Team FIFA Women's World Cup", "Maracanã, Rio de Janeiro", "Upcoming Edition", 0, 64, DataSourceType.VERIFIED_CURRENT),
            HistoricalWorldCupEdition(2023, TeamCategory.WOMENS_NATIONAL, "Australia / New Zealand", "🇦🇺🇳🇿", 32, "Spain Women", "🇪🇸", "England Women", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Sweden Women", "🇸🇪", "Australia Women", "Spain 1 - 0 England", "Stadium Australia, Sydney", "Hinata Miyazawa (5 goals)", 164, 64, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(2019, TeamCategory.WOMENS_NATIONAL, "France", "🇫🇷", 24, "USA Women", "🇺🇸", "Netherlands Women", "🇳🇱", "Sweden Women", "🇸🇪", "England Women", "USA 2 - 0 Netherlands", "Parc Olympique Lyonnais, Décines", "Megan Rapinoe / Alex Morgan / Ellen White (6)", 146, 52, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(2015, TeamCategory.WOMENS_NATIONAL, "Canada", "🇨🇦", 24, "USA Women", "🇺🇸", "Japan Women", "🇯🇵", "England Women", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Germany Women", "USA 5 - 2 Japan", "BC Place, Vancouver", "Celia Šašić / Carli Lloyd (6)", 146, 52, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(2011, TeamCategory.WOMENS_NATIONAL, "Germany", "🇩🇪", 16, "Japan Women", "🇯🇵", "USA Women", "🇺🇸", "Sweden Women", "🇸🇪", "France Women", "Japan 2 - 2 USA (3-1 pens)", "Waldstadion, Frankfurt", "Homare Sawa (5 goals)", 86, 32, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(2007, TeamCategory.WOMENS_NATIONAL, "China", "🇨🇳", 16, "Germany Women", "🇩🇪", "Brazil Women", "🇧🇷", "USA Women", "🇺🇸", "Norway Women", "Germany 2 - 0 Brazil", "Hongkou Football Stadium, Shanghai", "Marta (7 goals)", 111, 32, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(2003, TeamCategory.WOMENS_NATIONAL, "United States", "🇺🇸", 16, "Germany Women", "🇩🇪", "Sweden Women", "🇸🇪", "USA Women", "🇺🇸", "Canada Women", "Germany 2 - 1 Sweden (Golden Goal)", "Home Depot Center, Carson", "Birgit Prinz (7 goals)", 107, 32, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(1999, TeamCategory.WOMENS_NATIONAL, "United States", "🇺🇸", 16, "USA Women", "🇺🇸", "China Women", "🇨🇳", "Brazil Women", "🇧🇷", "Norway Women", "USA 0 - 0 China (5-4 pens)", "Rose Bowl, Pasadena", "Sun Wen / Sissi (7 goals)", 123, 32, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(1995, TeamCategory.WOMENS_NATIONAL, "Sweden", "🇸🇪", 12, "Norway Women", "🇳🇴", "Germany Women", "🇩🇪", "USA Women", "🇺🇸", "China Women", "Norway 2 - 0 Germany", "Råsunda Stadium, Solna", "Ann Kristin Aarønes (6 goals)", 99, 26, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(1991, TeamCategory.WOMENS_NATIONAL, "China", "🇨🇳", 12, "USA Women", "🇺🇸", "Norway Women", "🇳🇴", "Sweden Women", "🇸🇪", "Germany Women", "USA 2 - 1 Norway", "Tianhe Stadium, Guangzhou", "Michelle Akers (10 goals)", 99, 26, DataSourceType.VERIFIED_HISTORICAL),

            // YOUTH WORLD CUPS (U-20 & U-17 MEN'S AND WOMEN'S)
            HistoricalWorldCupEdition(2023, TeamCategory.U20_MENS_NATIONAL, "Argentina", "🇦🇷", 24, "Uruguay U-20", "🇺🇾", "Italy U-20", "🇮🇹", "Israel U-20", "🇮🇱", "South Korea U-20", "Uruguay 1 - 0 Italy", "Estadio Único Diego Armando Maradona, La Plata", "Cesare Casadei (7 goals)", 154, 52, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(2023, TeamCategory.U17_MENS_NATIONAL, "Indonesia", "🇮🇩", 24, "Germany U-17", "🇩🇪", "France U-17", "🇫🇷", "Mali U-17", "🇲🇱", "Argentina U-17", "Germany 2 - 2 France (4-3 pens)", "Manahan Stadium, Surakarta", "Agustín Ruberto (8 goals)", 172, 52, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(2024, TeamCategory.U20_WOMENS_NATIONAL, "Colombia", "🇨🇴", 24, "North Korea U-20W", "🇰🇵", "Japan U-20W", "🇯🇵", "USA U-20W", "🇺🇸", "Netherlands U-20W", "North Korea 1 - 0 Japan", "Estadio El Campín, Bogotá", "Choe Il-son (6 goals)", 147, 52, DataSourceType.VERIFIED_HISTORICAL),
            HistoricalWorldCupEdition(2024, TeamCategory.U17_WOMENS_NATIONAL, "Dominican Republic", "🇩🇴", 16, "North Korea U-17W", "🇰🇵", "Spain U-17W", "🇪🇸", "USA U-17W", "🇺🇸", "England U-17W", "North Korea 1 - 1 Spain (4-3 pens)", "Estadio Olímpico Félix Sánchez, Santo Domingo", "Kennedy Fuller (4 goals)", 94, 32, DataSourceType.VERIFIED_HISTORICAL)
        )
    }

    private fun buildVerifiedClubSeasonsHistory(): List<HistoricalClubSeasonRecord> {
        return listOf(
            // UEFA CHAMPIONS LEAGUE
            HistoricalClubSeasonRecord("ucl", "UEFA Champions League", Confederation.UEFA, "Europe", "2026-27", "In Progress (Swiss Phase)", "🇪🇺", "36 Participating Clubs", "36-Team League Phase + Knockout", "Active Season", "36-Team Swiss Format (8 Matchdays)", DataSourceType.VERIFIED_CURRENT, listOf("MD1-MD8 Swiss League Phase leading to Knockout Playoffs & R16")),
            HistoricalClubSeasonRecord("ucl", "UEFA Champions League", Confederation.UEFA, "Europe", "2024-25", "First 36-Team Swiss Edition", "🇪🇺", "UEFA Elite Clubs", "Swiss Table + Two-Legged KO", "Top Scorers", "36-Team Swiss League Phase", DataSourceType.VERIFIED_HISTORICAL, listOf("Liverpool topped inaugural 36-team League Phase table (21 pts)")),
            HistoricalClubSeasonRecord("ucl", "UEFA Champions League", Confederation.UEFA, "Europe", "2023-24", "Real Madrid", "🇪🇸", "Borussia Dortmund", "Real Madrid 2 - 0 Borussia Dortmund", "Harry Kane / Kylian Mbappé (8)", "32-Team Groups + Knockout", DataSourceType.VERIFIED_HISTORICAL, listOf("Final at Wembley: Real Madrid 2-0 Dortmund (Carvajal 74', Vinícius 83')", "SF: Real Madrid 4-3 Bayern Munich (agg)")),
            HistoricalClubSeasonRecord("ucl", "UEFA Champions League", Confederation.UEFA, "Europe", "2022-23", "Manchester City", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Inter Milan", "Manchester City 1 - 0 Inter Milan", "Erling Haaland (12 goals)", "32-Team Groups + Knockout", DataSourceType.VERIFIED_HISTORICAL, listOf("Final in Istanbul: Man City 1-0 Inter (Rodri 68')", "SF: Man City 5-1 Real Madrid (agg)")),
            HistoricalClubSeasonRecord("ucl", "UEFA Champions League", Confederation.UEFA, "Europe", "2021-22", "Real Madrid", "🇪🇸", "Liverpool", "Real Madrid 1 - 0 Liverpool", "Karim Benzema (15 goals)", "32-Team Groups + Knockout", DataSourceType.VERIFIED_HISTORICAL, listOf("Final in Paris: Real Madrid 1-0 Liverpool (Vinícius 59')", "SF: Real Madrid 6-5 Man City (AET)")),
            HistoricalClubSeasonRecord("ucl", "UEFA Champions League", Confederation.UEFA, "Europe", "2020-21", "Chelsea", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Manchester City", "Chelsea 1 - 0 Manchester City", "Erling Haaland (10 goals)", "32-Team Groups + Knockout", DataSourceType.VERIFIED_HISTORICAL, listOf("Final in Porto: Chelsea 1-0 Man City (Havertz 42')")),

            // UEFA EUROPA & CONFERENCE LEAGUE
            HistoricalClubSeasonRecord("uel", "UEFA Europa League", Confederation.UEFA, "Europe", "2023-24", "Atalanta", "🇮🇹", "Bayer Leverkusen", "Atalanta 3 - 0 Bayer Leverkusen", "Pierre-Emerick Aubameyang (10)", "Groups + Knockout", DataSourceType.VERIFIED_HISTORICAL, listOf("Final in Dublin: Atalanta 3-0 Leverkusen (Lookman hat-trick)")),
            HistoricalClubSeasonRecord("uecl", "UEFA Conference League", Confederation.UEFA, "Europe", "2023-24", "Olympiacos", "🇬🇷", "Fiorentina", "Olympiacos 1 - 0 Fiorentina (AET)", "Ayoub El Kaabi (11 goals)", "Groups + Knockout", DataSourceType.VERIFIED_HISTORICAL, listOf("Final in Athens: Olympiacos 1-0 Fiorentina (El Kaabi 116')")),

            // CONMEBOL LIBERTADORES & SUDAMERICANA
            HistoricalClubSeasonRecord("lib", "Copa Libertadores", Confederation.CONMEBOL, "South America", "2024", "Botafogo", "🇧🇷", "Atlético Mineiro", "Botafogo 3 - 1 Atlético Mineiro", "Júnior Santos (10 goals)", "32-Team Groups + Knockout", DataSourceType.VERIFIED_HISTORICAL, listOf("Final in Buenos Aires: Botafogo 3-1 Atlético Mineiro")),
            HistoricalClubSeasonRecord("lib", "Copa Libertadores", Confederation.CONMEBOL, "South America", "2023", "Fluminense", "🇧🇷", "Boca Juniors", "Fluminense 2 - 1 Boca Juniors (AET)", "Germán Cano (13 goals)", "32-Team Groups + Knockout", DataSourceType.VERIFIED_HISTORICAL, listOf("Final at Maracanã: Fluminense 2-1 Boca Juniors")),
            HistoricalClubSeasonRecord("sud", "Copa Sudamericana", Confederation.CONMEBOL, "South America", "2024", "Racing Club", "🇦🇷", "Cruzeiro", "Racing Club 3 - 1 Cruzeiro", "Adrián Martínez (10 goals)", "Groups + Knockout", DataSourceType.VERIFIED_HISTORICAL, listOf("Final in Asunción: Racing Club 3-1 Cruzeiro")),

            // CAF CHAMPIONS LEAGUE & CONFEDERATION CUP
            HistoricalClubSeasonRecord("cafcl", "CAF Champions League", Confederation.CAF, "Africa", "2023-24", "Al Ahly SC", "🇪🇬", "Espérance de Tunis", "Al Ahly 1 - 0 Espérance (agg)", "Hussein El Shahat (5)", "16-Team Groups + Two-Legged KO", DataSourceType.VERIFIED_HISTORICAL, listOf("1st Leg: Espérance 0-0 Al Ahly | 2nd Leg: Al Ahly 1-0 Espérance")),
            HistoricalClubSeasonRecord("cafcc", "CAF Confederation Cup", Confederation.CAF, "Africa", "2023-24", "Zamalek SC", "🇪🇬", "RS Berkane", "Zamalek 2 - 2 RS Berkane (away goals)", "Abdul Aziz Issah", "Groups + Two-Legged KO", DataSourceType.VERIFIED_HISTORICAL, listOf("2022-23 CAF Confederation Cup Champion: USM Alger (Algeria)")),
            HistoricalClubSeasonRecord("cafsc", "CAF Super Cup", Confederation.CAF, "Africa", "2023", "USM Alger", "🇩🇿", "Al Ahly SC", "USM Alger 1 - 0 Al Ahly SC", "Zineddine Belaïd (43' pen)", "Single Final", DataSourceType.VERIFIED_HISTORICAL, listOf("Final in Taif: USM Alger 1-0 Al Ahly")),

            // AFC, CONCACAF & NATIONAL LEAGUES
            HistoricalClubSeasonRecord("afccl", "AFC Champions League Elite", Confederation.AFC, "Asia", "2023-24", "Al Ain FC", "🇦🇪", "Yokohama F. Marinos", "Al Ain 6 - 3 Yokohama (agg)", "Soufiane Rahimi (13 goals)", "Groups + Two-Legged KO", DataSourceType.VERIFIED_HISTORICAL, listOf("2nd Leg Final: Al Ain 5-1 Yokohama F. Marinos")),
            HistoricalClubSeasonRecord("ccc", "CONCACAF Champions Cup", Confederation.CONCACAF, "North America", "2024", "CF Pachuca", "🇲🇽", "Columbus Crew", "CF Pachuca 3 - 0 Columbus Crew", "Salomón Rondón (9 goals)", "Knockout Bracket", DataSourceType.VERIFIED_HISTORICAL, listOf("Final: Pachuca 3-0 Columbus Crew")),
            HistoricalClubSeasonRecord("epl", "English Premier League", Confederation.UEFA, "England", "2023-24", "Manchester City", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Arsenal", "91 pts (Man City) vs 89 pts (Arsenal)", "Erling Haaland (27 goals)", "38 Matchdays Double Round Robin", DataSourceType.VERIFIED_HISTORICAL, listOf("Man City clinched 4th consecutive Premier League crown on final day")),
            HistoricalClubSeasonRecord("laliga", "La Liga", Confederation.UEFA, "Spain", "2023-24", "Real Madrid", "🇪🇸", "FC Barcelona", "95 pts (Real Madrid) vs 85 pts (Barcelona)", "Artem Dovbyk (24 goals)", "38 Matchdays Double Round Robin", DataSourceType.VERIFIED_HISTORICAL, listOf("Real Madrid won 36th La Liga title with only 1 league defeat")),
            HistoricalClubSeasonRecord("alg1", "Algerian Ligue 1 Mobilis", Confederation.CAF, "Algeria", "2023-24", "MC Alger", "🇩🇿", "CR Belouizdad", "65 pts (MC Alger) vs 53 pts (CRB) & 53 pts (CS Constantine)", "Youcef Belaïli / Ismaïl Belkacemi (14)", "30 Matchdays Double Round Robin", DataSourceType.VERIFIED_HISTORICAL, listOf("MC Alger won 8th Ligue 1 title; CR Belouizdad won 2024 Algerian Cup 1-0 vs MCA"))
        )
    }

    private fun buildPreloadedTournaments(allTeams: List<UtsTeam>): List<TournamentInstance> {
        val nationals = allTeams.filter { it.category == TeamCategory.MENS_NATIONAL }.sortedByDescending { it.overallRating }
        val clubs = allTeams.filter { it.category == TeamCategory.MENS_CLUB }.sortedByDescending { it.overallRating }

        // 1. 2026 FIFA World Cup (48 Teams, 12 Groups of 4)
        val wc2026Teams = nationals.take(48)
        val wc2026Id = "preloaded_wc_2026"
        val (wcGroups, wcDrawLog) = SimulationEngine.performPotBasedGroupDraw(
            teams = wc2026Teams,
            groupsCount = 12,
            teamsPerGroup = 4,
            hostTeamIds = wc2026Teams.filter { it.name in listOf("USA", "Canada", "Mexico") }.map { it.id },
            confederationProtection = true
        )
        val wc2026 = TournamentInstance(
            id = wc2026Id,
            name = "2026 FIFA Men's World Cup",
            subtitle = "USA • Canada • Mexico (48 Nations)",
            yearLabel = "2026",
            hostCountries = listOf("USA", "Canada", "Mexico"),
            sport = SportType.FOOTBALL,
            category = TeamCategory.MENS_NATIONAL,
            format = TournamentFormat.GROUP_PLUS_KNOCKOUT,
            dataSource = DataSourceType.VERIFIED_CURRENT,
            teamsCount = 48,
            groupsCount = 12,
            teamsPerGroup = 4,
            advancePerGroup = 2,
            bestThirdPlacedCount = 8,
            hasThirdPlaceMatch = true,
            participantTeamIds = wc2026Teams.map { it.id },
            groupsMap = wcGroups,
            matches = SimulationEngine.generateTournamentInitialFixtures(wc2026Id, TournamentFormat.GROUP_PLUS_KNOCKOUT, wc2026Teams, wcGroups),
            drawHistoryLog = wcDrawLog
        )

        // 2. UEFA Champions League 2026-27 (Swiss League Phase + Knockout)
        val uclClubs = clubs.filter { it.confederation == Confederation.UEFA }.take(36)
        val uclId = "preloaded_ucl_2026_27"
        val uclGroups = mapOf("Swiss Table" to uclClubs.map { it.id })
        val uclTourney = TournamentInstance(
            id = uclId,
            name = "UEFA Champions League 2026-27",
            subtitle = "36-Team Swiss League Phase + Knockout",
            yearLabel = "2026-27",
            hostCountries = listOf("Europe"),
            sport = SportType.FOOTBALL,
            category = TeamCategory.MENS_CLUB,
            format = TournamentFormat.SWISS_SYSTEM,
            dataSource = DataSourceType.VERIFIED_CURRENT,
            teamsCount = uclClubs.size,
            groupsCount = 1,
            teamsPerGroup = uclClubs.size,
            advancePerGroup = 16,
            hasThirdPlaceMatch = false,
            twoLeggedKnockout = true,
            participantTeamIds = uclClubs.map { it.id },
            groupsMap = uclGroups,
            matches = SimulationEngine.generateTournamentInitialFixtures(uclId, TournamentFormat.SWISS_SYSTEM, uclClubs, uclGroups, twoLeggedKnockout = true),
            drawHistoryLog = listOf("36 European clubs seeded into the 8-matchday Swiss League Phase")
        )

        // 3. World Super League (20 Elite Global Clubs)
        val superClubs = clubs.take(20)
        val wslId = "preloaded_world_super_league"
        val wslGroups = mapOf("League Table" to superClubs.map { it.id })
        val wslTourney = TournamentInstance(
            id = wslId,
            name = "World Super League",
            subtitle = "20 Elite Global Clubs • Double Round Robin",
            yearLabel = "2026-27",
            hostCountries = listOf("Global"),
            sport = SportType.FOOTBALL,
            category = TeamCategory.MENS_CLUB,
            format = TournamentFormat.DOUBLE_ROUND_ROBIN,
            dataSource = DataSourceType.USER_CUSTOM,
            teamsCount = superClubs.size,
            groupsCount = 1,
            teamsPerGroup = superClubs.size,
            participantTeamIds = superClubs.map { it.id },
            groupsMap = wslGroups,
            matches = SimulationEngine.generateTournamentInitialFixtures(wslId, TournamentFormat.DOUBLE_ROUND_ROBIN, superClubs, wslGroups)
        )

        return listOf(wc2026, uclTourney, wslTourney)
    }

    private fun buildDefaultSeasonPyramids(allTeams: List<UtsTeam>): SeasonModeState {
        val clubs = allTeams.filter { it.category == TeamCategory.MENS_CLUB }
        val divisions = mutableListOf<DivisionModel>()

        fun addPyramidDivision(
            id: String,
            country: String,
            flag: String,
            confed: Confederation,
            tier: Int,
            name: String,
            autoProm: Int,
            playoffProm: Int,
            rel: Int
        ) {
            val tierTeams = clubs.filter { it.country == country && it.domesticTier == tier }
            if (tierTeams.size >= 4) {
                val tIds = tierTeams.map { it.id }
                divisions.add(
                    DivisionModel(
                        id = id,
                        country = country,
                        flagEmoji = flag,
                        confederation = confed,
                        tierLevel = tier,
                        name = name,
                        teamIds = tIds,
                        autoPromotionSlots = autoProm,
                        playoffPromotionSlots = playoffProm,
                        relegationSlots = rel,
                        matches = SimulationEngine.generateTournamentInitialFixtures(
                            tournamentId = id,
                            format = TournamentFormat.DOUBLE_ROUND_ROBIN,
                            teams = tierTeams,
                            groupsMap = mapOf(name to tIds)
                        )
                    )
                )
            }
        }

        // England 5-Tier Pyramid
        addPyramidDivision("eng_d1", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", Confederation.UEFA, 1, "Premier League", 0, 0, 3)
        addPyramidDivision("eng_d2", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", Confederation.UEFA, 2, "EFL Championship", 2, 1, 3)
        addPyramidDivision("eng_d3", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", Confederation.UEFA, 3, "EFL League One", 2, 1, 3)
        addPyramidDivision("eng_d4", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", Confederation.UEFA, 4, "EFL League Two", 3, 1, 2)
        addPyramidDivision("eng_d5", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", Confederation.UEFA, 5, "National League", 1, 1, 0)

        // Algeria 2-Tier Pyramid
        addPyramidDivision("alg_d1", "Algeria", "🇩🇿", Confederation.CAF, 1, "Ligue 1 Mobilis", 0, 0, 2)
        addPyramidDivision("alg_d2", "Algeria", "🇩🇿", Confederation.CAF, 2, "Algerian Ligue 2", 2, 0, 0)

        // Spain 2-Tier Pyramid
        addPyramidDivision("esp_d1", "Spain", "🇪🇸", Confederation.UEFA, 1, "La Liga", 0, 0, 3)
        addPyramidDivision("esp_d2", "Spain", "🇪🇸", Confederation.UEFA, 2, "Segunda División", 2, 1, 0)

        // Germany 2-Tier Pyramid
        addPyramidDivision("ger_d1", "Germany", "🇩🇪", Confederation.UEFA, 1, "Bundesliga", 0, 0, 2)
        addPyramidDivision("ger_d2", "Germany", "🇩🇪", Confederation.UEFA, 2, "2. Bundesliga", 2, 1, 0)

        // Italy 2-Tier Pyramid
        addPyramidDivision("ita_d1", "Italy", "🇮🇹", Confederation.UEFA, 1, "Serie A", 0, 0, 3)
        addPyramidDivision("ita_d2", "Italy", "🇮🇹", Confederation.UEFA, 2, "Serie B", 2, 1, 0)

        // France 2-Tier Pyramid
        addPyramidDivision("fra_d1", "France", "🇫🇷", Confederation.UEFA, 1, "Ligue 1", 0, 0, 2)
        addPyramidDivision("fra_d2", "France", "🇫🇷", Confederation.UEFA, 2, "Ligue 2", 2, 1, 0)

        // Domestic Cups (FA Cup & Algerian Cup)
        val engClubs = clubs.filter { it.country == "England" }.take(16)
        val faCup = TournamentInstance(
            id = "cup_eng_fa_2026",
            name = "English FA Cup",
            subtitle = "Multi-Division Knockout Cup",
            yearLabel = "2026-27",
            hostCountries = listOf("England"),
            category = TeamCategory.MENS_CLUB,
            format = TournamentFormat.SINGLE_ELIMINATION,
            teamsCount = engClubs.size,
            hasThirdPlaceMatch = false,
            participantTeamIds = engClubs.map { it.id },
            matches = SimulationEngine.generateKnockoutRoundMatches("cup_eng_fa_2026", engClubs.shuffled(), 1, false)
        )

        val algClubs = clubs.filter { it.country == "Algeria" }.take(16)
        val algCup = TournamentInstance(
            id = "cup_alg_2026",
            name = "Algerian Cup (Coupe d'Algérie)",
            subtitle = "National Knockout Cup",
            yearLabel = "2026-27",
            hostCountries = listOf("Algeria"),
            category = TeamCategory.MENS_CLUB,
            format = TournamentFormat.SINGLE_ELIMINATION,
            teamsCount = algClubs.size,
            hasThirdPlaceMatch = false,
            participantTeamIds = algClubs.map { it.id },
            matches = SimulationEngine.generateKnockoutRoundMatches("cup_alg_2026", algClubs.shuffled(), 1, false)
        )

        // Continental Cups in Season Mode
        val uefaTop = clubs.filter { it.confederation == Confederation.UEFA }.sortedByDescending { it.overallRating }.take(16)
        val (uclGroups, uclLog) = SimulationEngine.performPotBasedGroupDraw(uefaTop, 4, 4, confederationProtection = false)
        val seasonUcl = TournamentInstance(
            id = "season_ucl_2026",
            name = "UEFA Champions League 2026/27",
            subtitle = "Continental Club Championship",
            yearLabel = "2026-27",
            hostCountries = listOf("Europe"),
            category = TeamCategory.MENS_CLUB,
            format = TournamentFormat.GROUP_PLUS_KNOCKOUT,
            teamsCount = 16,
            groupsCount = 4,
            teamsPerGroup = 4,
            hasThirdPlaceMatch = false,
            twoLeggedKnockout = true,
            participantTeamIds = uefaTop.map { it.id },
            groupsMap = uclGroups,
            matches = SimulationEngine.generateTournamentInitialFixtures("season_ucl_2026", TournamentFormat.GROUP_PLUS_KNOCKOUT, uefaTop, uclGroups, twoLeggedKnockout = true),
            drawHistoryLog = uclLog
        )

        return SeasonModeState(
            seasonYear = 2026,
            dynamicRatingsEnabled = true,
            activeCountry = "England",
            divisions = divisions,
            domesticCups = listOf(faCup, algCup),
            continentalCups = listOf(seasonUcl),
            seasonHistoryLogs = listOf("Season 2026/27 initialized with multi-tier pyramids, domestic cups, and continental qualification.")
        )
    }
}
