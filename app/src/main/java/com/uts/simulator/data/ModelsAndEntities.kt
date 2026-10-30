package com.uts.simulator.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class SportType(
    val displayName: String,
    val supportsDraws: Boolean,
    val defaultPeriods: Int,
    val periodName: String,
    val scoringUnit: String
) {
    FOOTBALL("Football", true, 2, "Half", "Goals"),
    BASKETBALL("Basketball", false, 4, "Quarter", "Points"),
    TENNIS("Tennis", false, 3, "Set", "Sets"),
    VOLLEYBALL("Volleyball", false, 5, "Set", "Sets"),
    HANDBALL("Handball", true, 2, "Half", "Goals"),
    RUGBY("Rugby", true, 2, "Half", "Points"),
    CRICKET("Cricket", false, 2, "Innings", "Runs"),
    ESPORTS("Esports", false, 3, "Map", "Maps"),
    BOXING("Boxing", true, 12, "Round", "Points"),
    MMA("MMA", false, 5, "Round", "Rounds"),
    TABLE_TENNIS("Table Tennis", false, 5, "Game", "Games"),
    BADMINTON("Badminton", false, 3, "Game", "Games"),
    BASEBALL("Baseball", false, 9, "Inning", "Runs")
}

enum class Confederation(val code: String, val fullName: String, val region: String) {
    AFC("AFC", "Asian Football Confederation", "Asia"),
    CAF("CAF", "Confederation of African Football", "Africa"),
    CONCACAF("CONCACAF", "Confederation of North, Central America & Caribbean", "North & Central America"),
    CONMEBOL("CONMEBOL", "South American Football Confederation", "South America"),
    OFC("OFC", "Oceania Football Confederation", "Oceania"),
    UEFA("UEFA", "Union of European Football Associations", "Europe"),
    GLOBAL("FIFA", "Global / Intercontinental", "World")
}

enum class TeamCategory(val label: String) {
    NATIONAL_MEN("Men's National Team"),
    NATIONAL_WOMEN("Women's National Team"),
    NATIONAL_U20_MEN("FIFA U-20 Men"),
    NATIONAL_U17_MEN("FIFA U-17 Men"),
    NATIONAL_U20_WOMEN("FIFA U-20 Women"),
    NATIONAL_U17_WOMEN("FIFA U-17 Women"),
    CLUB_MEN("Men's Football Club"),
    MULTI_SPORT("Multi-Sport Team")
}

enum class DataSourceType(val badge: String, val description: String) {
    VERIFIED_HISTORICAL("VERIFIED HISTORICAL", "Official archived competition results & records"),
    VERIFIED_CURRENT("VERIFIED 2026-27", "Current real-world competition structure & teams"),
    USER_CUSTOM("CUSTOM CREATED", "User-created custom tournament or what-if simulation"),
    SIMULATED_FUTURE("SEASON SIMULATED", "Generated during multi-season progression")
}

enum class TournamentFormat(val label: String, val description: String) {
    GROUP_AND_KNOCKOUT("Group Stage + Knockout", "World Cup / Continental format with groups advancing to bracket"),
    SINGLE_ELIMINATION("Single Elimination", "Direct knockout tournament bracket"),
    DOUBLE_ELIMINATION("Double Elimination", "Winners Bracket & Losers Bracket format"),
    ROUND_ROBIN("Single Round Robin", "Every team plays every other team once"),
    DOUBLE_ROUND_ROBIN("Double Round Robin", "Home and away league format"),
    MULTIPLE_ROUND_ROBIN("Triple/Quad Round Robin", "Multiple loop league season"),
    SWISS_SYSTEM("Swiss System (League Phase)", "36-team modern Champions League style Swiss phase + knockouts"),
    TWO_LEGGED_KNOCKOUT("Two-Legged Knockout", "Home and away aggregate ties with single final"),
    BEST_OF_3("Best of 3 Series", "First team to win 2 games advances"),
    BEST_OF_5("Best of 5 Series", "First team to win 3 games advances"),
    BEST_OF_7("Best of 7 Series", "First team to win 4 games advances"),
    SURVIVAL("Survival Elimination", "Lowest scoring teams eliminated after each matchday"),
    BATTLE_ROYALE("Battle Royale", "All teams clash in rapid multi-stage elimination rounds")
}

enum class GameMode(val title: String, val subtitle: String) {
    STANDARD("Standard Competition", "Authentic tournament or league rules"),
    WHAT_IF("What-If Historical Mode", "Replay a historical World Cup or UCL with altered teams or seedings"),
    FANTASY("Fantasy Super Tournament", "Custom drafted squads and boosted ratings"),
    WORLD_TOURNAMENT("World Club / Country Clash", "Cross-continental mega competition"),
    SURVIVAL_MODE("Survival Mode", "Bottom teams eliminated every round"),
    BATTLE_ROYALE_MODE("Battle Royale", "High-chaos multi-round elimination until 1 champion remains"),
    COUNTRY_VS_COUNTRY("Country vs Country", "International showdown"),
    CLUB_VS_CLUB("Club vs Club Global", "Cross-border club competition"),
    HISTORICAL_RECREATION("Historical Recreation", "Replay classic editions from 1930 to 2026")
}

enum class SimulationSpeed(val label: String, val delayMs: Long) {
    INSTANT("Instant", 0L),
    FAST("Fast", 40L),
    REALISTIC("Realistic", 120L),
    ANIMATED("Live Animated", 260L)
}

@Entity(
    tableName = "teams",
    indices = [
        Index(value = ["confederation"]),
        Index(value = ["country"]),
        Index(value = ["category"]),
        Index(value = ["overallRating"])
    ]
)
data class TeamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val shortName: String,
    val country: String,
    val confederation: Confederation,
    val category: TeamCategory,
    val sport: SportType = SportType.FOOTBALL,
    val flagEmoji: String = "🏳️",
    val primaryColorHex: String = "#35D07F",
    val secondaryColorHex: String = "#191F2B",
    val customLogoUri: String? = null,
    val stadiumName: String = "National Stadium",
    val domesticDivisionTier: Int = 1,
    val overallRating: Int = 75,
    val attackRating: Int = 75,
    val midfieldRating: Int = 75,
    val defenseRating: Int = 75,
    val goalkeeperRating: Int = 75,
    val homeAdvantage: Int = 4,
    val formRating: Int = 75,
    val previousRating: Int = 75,
    val ratingHistoryJson: String = "[75]",
    val currentStreak: Int = 0,
    val titlesWon: Int = 0,
    val isCustom: Boolean = false
)

@Entity(
    tableName = "historical_records",
    indices = [Index(value = ["competitionCategory"]), Index(value = ["yearLabel"])]
)
data class HistoricalEditionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val competitionCode: String,
    val competitionName: String,
    val competitionCategory: String, // MENS_WC, WOMENS_WC, U20_WC, U17_WC, U20_WWC, U17_WWC, UCL, UEL, UECL, LIBERTADORES, CAF_CL, AFC_CL, PREMIER_LEAGUE, LA_LIGA, LIGUE1_DZ, etc.
    val yearLabel: String,
    val hostCountry: String,
    val participantCount: Int,
    val champion: String,
    val runnerUp: String,
    val thirdPlace: String,
    val fourthPlace: String,
    val finalScoreText: String,
    val finalVenue: String,
    val topScorer: String,
    val totalGoals: Int,
    val totalMatches: Int,
    val dataSourceType: DataSourceType = DataSourceType.VERIFIED_HISTORICAL,
    val participatingTeamsJson: String = "[]",
    val notableMatchesJson: String = "[]",
    val fullFixturesAvailable: Boolean = true,
    val availabilityNote: String = "Verified official tournament record & summary results."
)

data class HistoricalMatchSummary(
    val stage: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val penaltyNote: String = ""
)

@Entity(
    tableName = "tournaments",
    indices = [Index(value = ["status"]), Index(value = ["dataSourceType"])]
)
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val yearSeason: String,
    val sport: SportType = SportType.FOOTBALL,
    val format: TournamentFormat = TournamentFormat.GROUP_AND_KNOCKOUT,
    val gameMode: GameMode = GameMode.STANDARD,
    val dataSourceType: DataSourceType = DataSourceType.USER_CUSTOM,
    val categoryTag: String = "INTERNATIONAL", // INTERNATIONAL, CLUB, SEASON_LEAGUE, SEASON_CUP, QUALIFICATION
    val hostCountries: String = "International",
    val finalVenue: String = "National Arena",
    val participantTeamIdsJson: String = "[]",
    val groupCount: Int = 8,
    val teamsPerGroup: Int = 4,
    val qualifiedPerGroup: Int = 2,
    val bestThirdCount: Int = 0,
    val hasThirdPlaceMatch: Boolean = true,
    val twoLeggedKnockout: Boolean = false,
    val pointsForWin: Int = 3,
    val pointsForDraw: Int = 1,
    val pointsForLoss: Int = 0,
    val currentStage: String = "GROUP_STAGE", // QUALIFICATION, GROUP_STAGE, ROUND_OF_32, ROUND_OF_16, QUARTER_FINALS, SEMI_FINALS, THIRD_PLACE, FINAL, COMPLETED
    val status: String = "ACTIVE", // ACTIVE, COMPLETED
    val championTeamId: Long? = null,
    val runnerUpTeamId: Long? = null,
    val thirdPlaceTeamId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val seasonSaveId: Long? = null,
    val divisionTier: Int = 1,
    val countryName: String = "Global",
    val promotedCount: Int = 3,
    val relegatedCount: Int = 3,
    val playoffCount: Int = 4
)

data class MatchEventItem(
    val minute: Int,
    val type: String, // GOAL, OWN_GOAL, PENALTY_GOAL, YELLOW_CARD, RED_CARD, INJURY, SUBSTITUTION, PERIOD_END
    val teamId: Long,
    val teamName: String,
    val playerName: String,
    val description: String
)

@Entity(
    tableName = "matches",
    indices = [
        Index(value = ["tournamentId"]),
        Index(value = ["stage"]),
        Index(value = ["groupName"]),
        Index(value = ["isPlayed"])
    ]
)
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tournamentId: Long,
    val sport: SportType = SportType.FOOTBALL,
    val stage: String, // GROUP, LEAGUE, R128, R64, R32, R16, QF, SF, THIRD_PLACE, FINAL, WINNERS_R1, LOSERS_R1, SWISS, QUAL_GROUP, QUAL_PLAYOFF
    val roundNumber: Int = 1,
    val groupName: String = "A",
    val matchOrder: Int = 1,
    val homeTeamId: Long,
    val awayTeamId: Long,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeFlag: String = "🏳️",
    val awayFlag: String = "🏳️",
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homeExtraTimeScore: Int? = null,
    val awayExtraTimeScore: Int? = null,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
    val isExtraTime: Boolean = false,
    val isPenalties: Boolean = false,
    val isPlayed: Boolean = false,
    val legNumber: Int = 1,
    val seriesHomeWins: Int = 0,
    val seriesAwayWins: Int = 0,
    val homePossession: Int = 50,
    val awayPossession: Int = 50,
    val homeShots: Int = 0,
    val awayShots: Int = 0,
    val homeShotsOnTarget: Int = 0,
    val awayShotsOnTarget: Int = 0,
    val homeCorners: Int = 0,
    val awayCorners: Int = 0,
    val homeFouls: Int = 0,
    val awayFouls: Int = 0,
    val homeYellowCards: Int = 0,
    val awayYellowCards: Int = 0,
    val homeRedCards: Int = 0,
    val awayRedCards: Int = 0,
    val eventsJson: String = "[]",
    val periodScoresJson: String = "[]",
    val venue: String = "Main Stadium",
    val winnerTeamId: Long? = null
)

data class StandingsRow(
    val teamId: Long,
    val teamName: String,
    val flagEmoji: String,
    val groupName: String,
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val goalDifference: Int = 0,
    val points: Int = 0,
    val form: String = "",
    val zoneLabel: String = "" // PROMOTED, UCL, UEL, PLAYOFF, RELEGATED
)

@Entity(tableName = "season_saves")
data class SeasonSaveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saveName: String,
    val currentSeasonLabel: String = "2026-27",
    val countriesIncludedJson: String = "[\"England\",\"Spain\",\"Germany\",\"Italy\",\"France\",\"Algeria\"]",
    val dynamicRatingsEnabled: Boolean = true,
    val continentalCupsEnabled: Boolean = true,
    val domesticCupsEnabled: Boolean = true,
    val uclSlotPerCountry: Int = 4,
    val uelSlotPerCountry: Int = 2,
    val seasonHistoryLogsJson: String = "[]",
    val lastUpdated: Long = System.currentTimeMillis()
)

data class SeasonRolloverLog(
    val seasonLabel: String,
    val summaryLines: List<String>
)

data class ConfederationQualConfig(
    val confederation: Confederation,
    val directSlots: Int,
    val playoffSlots: Int,
    val groupCount: Int
)

data class DrawPotAssignment(
    val potNumber: Int,
    val teams: List<TeamEntity>
)

data class DrawStepLog(
    val stepIndex: Int,
    val team: TeamEntity,
    val assignedGroup: String,
    val note: String
)

class UtsTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromSportType(value: SportType): String = value.name

    @TypeConverter
    fun toSportType(value: String): SportType =
        runCatching { SportType.valueOf(value) }.getOrDefault(SportType.FOOTBALL)

    @TypeConverter
    fun fromConfederation(value: Confederation): String = value.name

    @TypeConverter
    fun toConfederation(value: String): Confederation =
        runCatching { Confederation.valueOf(value) }.getOrDefault(Confederation.GLOBAL)

    @TypeConverter
    fun fromTeamCategory(value: TeamCategory): String = value.name

    @TypeConverter
    fun toTeamCategory(value: String): TeamCategory =
        runCatching { TeamCategory.valueOf(value) }.getOrDefault(TeamCategory.NATIONAL_MEN)

    @TypeConverter
    fun fromDataSourceType(value: DataSourceType): String = value.name

    @TypeConverter
    fun toDataSourceType(value: String): DataSourceType =
        runCatching { DataSourceType.valueOf(value) }.getOrDefault(DataSourceType.USER_CUSTOM)

    @TypeConverter
    fun fromTournamentFormat(value: TournamentFormat): String = value.name

    @TypeConverter
    fun toTournamentFormat(value: String): TournamentFormat =
        runCatching { TournamentFormat.valueOf(value) }.getOrDefault(TournamentFormat.GROUP_AND_KNOCKOUT)

    @TypeConverter
    fun fromGameMode(value: GameMode): String = value.name

    @TypeConverter
    fun toGameMode(value: String): GameMode =
        runCatching { GameMode.valueOf(value) }.getOrDefault(GameMode.STANDARD)

    fun parseIdList(json: String): List<Long> {
        val type = object : TypeToken<List<Long>>() {}.type
        return runCatching { gson.fromJson<List<Long>>(json, type) ?: emptyList() }.getOrDefault(emptyList())
    }

    fun toIdListJson(ids: List<Long>): String = gson.toJson(ids)

    fun parseEvents(json: String): List<MatchEventItem> {
        val type = object : TypeToken<List<MatchEventItem>>() {}.type
        return runCatching { gson.fromJson<List<MatchEventItem>>(json, type) ?: emptyList() }.getOrDefault(emptyList())
    }

    fun toEventsJson(events: List<MatchEventItem>): String = gson.toJson(events)

    fun parseHistoricalMatches(json: String): List<HistoricalMatchSummary> {
        val type = object : TypeToken<List<HistoricalMatchSummary>>() {}.type
        return runCatching { gson.fromJson<List<HistoricalMatchSummary>>(json, type) ?: emptyList() }.getOrDefault(emptyList())
    }

    fun toHistoricalMatchesJson(matches: List<HistoricalMatchSummary>): String = gson.toJson(matches)

    fun parseRatingHistory(json: String): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return runCatching { gson.fromJson<List<Int>>(json, type) ?: listOf(75) }.getOrDefault(listOf(75))
    }

    fun toRatingHistoryJson(history: List<Int>): String = gson.toJson(history.takeLast(25))
}
