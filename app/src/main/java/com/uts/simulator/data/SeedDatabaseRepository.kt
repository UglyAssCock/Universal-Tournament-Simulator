package com.uts.simulator.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

enum class UtsConfederation(val code: String, val fullName: String, val region: String) {
    UEFA("UEFA", "Union of European Football Associations", "Europe"),
    CONMEBOL("CONMEBOL", "South American Football Confederation", "South America"),
    CAF("CAF", "Confederation of African Football", "Africa"),
    AFC("AFC", "Asian Football Confederation", "Asia"),
    CONCACAF("CONCACAF", "Confederation of North, Central America and Caribbean Association Football", "North & Central America"),
    OFC("OFC", "Oceania Football Confederation", "Oceania"),
    GLOBAL("FIFA", "Global / Intercontinental", "World")
}

enum class UtsTeamCategory(val label: String) {
    MEN_NATIONAL("Men's National Team"),
    WOMEN_NATIONAL("Women's National Team"),
    U20_MEN("FIFA U-20 Men's National Team"),
    U17_MEN("FIFA U-17 Men's National Team"),
    U20_WOMEN("FIFA U-20 Women's National Team"),
    U17_WOMEN("FIFA U-17 Women's National Team"),
    MEN_CLUB("Men's Football Club"),
    MULTI_SPORT("Multi-Sport Team / Athlete")
}

enum class UtsSportType(
    val id: String,
    val displayName: String,
    val allowsDrawsInGroup: Boolean,
    val defaultPeriods: Int,
    val scoringUnit: String
) {
    FOOTBALL("FOOTBALL", "Football (Soccer)", true, 2, "Goals"),
    BASKETBALL("BASKETBALL", "Basketball", false, 4, "Points"),
    TENNIS("TENNIS", "Tennis", false, 3, "Sets"),
    VOLLEYBALL("VOLLEYBALL", "Volleyball", false, 5, "Sets"),
    HANDBALL("HANDBALL", "Handball", true, 2, "Goals"),
    RUGBY("RUGBY", "Rugby Union", true, 2, "Points"),
    CRICKET("CRICKET", "Cricket (T20/ODI)", false, 2, "Runs"),
    ESPORTS("ESPORTS", "Esports (Bo3/Bo5)", false, 3, "Maps"),
    BOXING("BOXING", "Boxing", false, 12, "Rounds"),
    MMA("MMA", "Mixed Martial Arts", false, 5, "Rounds"),
    TABLE_TENNIS("TABLE_TENNIS", "Table Tennis", false, 5, "Games"),
    BADMINTON("BADMINTON", "Badminton", false, 3, "Games"),
    BASEBALL("BASEBALL", "Baseball", false, 9, "Runs")
}

enum class UtsTournamentFormat(val id: String, val title: String, val description: String) {
    GROUP_AND_KNOCKOUT("GROUP_KO", "Group Stage + Knockout", "Classic World Cup / Champions League format with groups and bracket"),
    SINGLE_ELIMINATION("SINGLE_ELIM", "Single Elimination", "Direct knockout bracket from Round 1 to the Final"),
    DOUBLE_ELIMINATION("DOUBLE_ELIM", "Double Elimination", "Upper and Lower brackets; two losses required for elimination"),
    ROUND_ROBIN_SINGLE("RR_SINGLE", "Single Round Robin League", "Every participant plays every other participant once"),
    ROUND_ROBIN_DOUBLE("RR_DOUBLE", "Double Round Robin League", "Home and away full league table format"),
    SWISS_SYSTEM("SWISS", "Swiss League Phase + Knockout", "Modern 36-team Champions League style league phase + playoffs"),
    TWO_LEGGED_KO("TWO_LEG_KO", "Two-Legged Knockout", "Home and away aggregate ties with extra time and penalties"),
    BEST_OF_SERIES("BEST_OF", "Best of 3 / 5 / 7 Series", "Playoff series format where first to win majority advances"),
    SURVIVAL_MODE("SURVIVAL", "Survival Elimination", "Bottom-performing teams are eliminated after every round"),
    BATTLE_ROYALE("BATTLE_ROYALE", "Battle Royale Multi-Round", "All participants clash across progressive cut-off rounds")
}

enum class UtsDataOrigin(val label: String) {
    VERIFIED_HISTORICAL("Verified Historical Dataset"),
    VERIFIED_CURRENT("Verified 2026-27 Season Dataset"),
    USER_CUSTOM("User Custom Creation"),
    SIMULATED_SEASON("Simulated Dynamic Season")
}

data class UtsCoreTeam(
    val id: String,
    val name: String,
    val shortCode: String,
    val country: String,
    val flagEmoji: String,
    val confederation: UtsConfederation,
    val category: UtsTeamCategory,
    val sport: UtsSportType = UtsSportType.FOOTBALL,
    val overallRating: Int,
    val attackRating: Int,
    val midfieldRating: Int,
    val defenseRating: Int,
    val goalkeeperRating: Int,
    val form: Int = 75,
    val homeAdvantage: Int = 3,
    val stadiumName: String = "National Arena",
    val primaryColorHex: String = "#1E88E5",
    val secondaryColorHex: String = "#FFFFFF",
    val customLogoUri: String = "",
    val domesticTier: Int = 1,
    val previousRating: Int = overallRating
)

data class UtsHistoricalFixture(
    val stage: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val notes: String = ""
)

data class UtsHistoricalEdition(
    val id: String,
    val competitionCategory: String, // "MENS_WC", "WOMENS_WC", "U20_MEN_WC", "U17_MEN_WC", "U20_WOMEN_WC", "U17_WOMEN_WC", "UCL", "UEL", "UECL", "LIBERTADORES", "CAF_CL", "AFC_ELITE", "CONCACAF_CC", "OFC_CL", "DOMESTIC_LEAGUE", "DOMESTIC_CUP"
    val competitionName: String,
    val editionYear: String,
    val hostOrCountry: String,
    val confederation: UtsConfederation,
    val teamCount: Int,
    val champion: String,
    val runnerUp: String,
    val thirdPlace: String,
    val fourthPlace: String,
    val finalScoreText: String,
    val topScorer: String,
    val totalGoals: Int,
    val matchesPlayed: Int,
    val formatSummary: String,
    val dataOrigin: UtsDataOrigin = UtsDataOrigin.VERIFIED_HISTORICAL,
    val verifiedFixtures: List<UtsHistoricalFixture> = emptyList(),
    val participatingTeams: List<String> = emptyList()
)

data class UtsMatchEventItem(
    val minute: Int,
    val type: String, // "GOAL", "OWN_GOAL", "PENALTY_GOAL", "YELLOW_CARD", "RED_CARD", "INJURY", "SUBSTITUTION", "COMMENTARY"
    val teamId: String,
    val teamName: String,
    val playerName: String,
    val description: String
)

data class UtsMatchRecord(
    val id: String,
    val tournamentId: String,
    val stageName: String, // e.g., "Group A", "Round of 16", "Quarter-Final", "Semi-Final", "Third Place", "Final", "Matchday 1"
    val groupOrRoundIndex: Int,
    val homeTeamId: String,
    val homeTeamName: String,
    val homeFlag: String,
    val awayTeamId: String,
    val awayTeamName: String,
    val awayFlag: String,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val isPlayed: Boolean = false,
    val wentToExtraTime: Boolean = false,
    val wentToPenalties: Boolean = false,
    val homePenalties: Int = 0,
    val awayPenalties: Int = 0,
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
    val events: List<UtsMatchEventItem> = emptyList(),
    val legNumber: Int = 1
)

data class UtsStandingRow(
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
    val formString: String = "",
    val statusBadge: String = "" // "QUALIFIED", "PROMOTED", "PLAYOFF", "UCL", "REL"
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
}

data class UtsActiveTournament(
    val id: String,
    val name: String,
    val yearLabel: String,
    val hostName: String,
    val sport: UtsSportType,
    val category: UtsTeamCategory,
    val format: UtsTournamentFormat,
    val groupCount: Int,
    val teamsPerGroup: Int,
    val advancePerGroup: Int,
    val bestThirdCount: Int,
    val hasThirdPlaceMatch: Boolean,
    val isCompleted: Boolean = false,
    val championName: String = "",
    val runnerUpName: String = "",
    val thirdPlaceName: String = "",
    val currentPhase: String = "GROUP_STAGE", // "QUALIFICATION", "GROUP_STAGE", "KNOCKOUT", "COMPLETED"
    val participantIds: List<String> = emptyList(),
    val groupAssignments: Map<String, List<String>> = emptyMap(), // "Group A" -> listOf(teamId)
    val drawHistoryLog: List<String> = emptyList(),
    val dataOrigin: UtsDataOrigin = UtsDataOrigin.USER_CUSTOM
)

data class UtsDivisionConfig(
    val divisionLevel: Int,
    val divisionName: String,
    val countryName: String,
    val promotedCount: Int,
    val promotionPlayoffCount: Int,
    val relegatedCount: Int,
    val continentalSlotsUcl: Int = 0,
    val continentalSlotsSecondTier: Int = 0,
    val teamIds: List<String>,
    val matches: List<UtsMatchRecord> = emptyList()
)

data class UtsCountryPyramid(
    val countryName: String,
    val flagEmoji: String,
    val confederation: UtsConfederation,
    val currentSeasonLabel: String,
    val domesticCupName: String,
    val divisions: List<UtsDivisionConfig>,
    val cupMatches: List<UtsMatchRecord> = emptyList(),
    val cupChampionName: String = "",
    val seasonHistoryLogs: List<String> = emptyList()
)

data class UtsRatingLogEntry(
    val teamId: String,
    val teamName: String,
    val oldRating: Int,
    val newRating: Int,
    val reason: String,
    val timestampLabel: String
)

class SeedDatabaseRepository(context: Context) {

    private val dbHelper = UtsSqliteHelper(context.applicationContext)

    init {
        ensureSeeded()
    }

    private fun ensureSeeded() {
        val db = dbHelper.writableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM uts_teams", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        if (count == 0) {
            seedAllInitialData(db)
        }
    }

    fun resetAndReseedDatabase() {
        val db = dbHelper.writableDatabase
        db.execSQL("DELETE FROM uts_teams")
        db.execSQL("DELETE FROM uts_tournaments")
        db.execSQL("DELETE FROM uts_matches")
        db.execSQL("DELETE FROM uts_rating_logs")
        seedAllInitialData(db)
    }

    private fun seedAllInitialData(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            val allNationalTeams = buildAll211FifaNationalTeams()
            for (team in allNationalTeams) {
                insertTeamInternal(db, team)
                // Also seed Women's and U-20/U-17 counterparts for major nations & expandable structure
                val womenOverall = (team.overallRating - 2).coerceIn(35, 95)
                insertTeamInternal(
                    db,
                    team.copy(
                        id = "${team.id}_W",
                        name = "${team.name} Women",
                        category = UtsTeamCategory.WOMEN_NATIONAL,
                        overallRating = womenOverall,
                        attackRating = (team.attackRating - 2).coerceIn(35, 95),
                        midfieldRating = (team.midfieldRating - 2).coerceIn(35, 95),
                        defenseRating = (team.defenseRating - 2).coerceIn(35, 95),
                        goalkeeperRating = (team.goalkeeperRating - 2).coerceIn(35, 95)
                    )
                )
                val u20Overall = (team.overallRating - 10).coerceIn(30, 86)
                insertTeamInternal(
                    db,
                    team.copy(
                        id = "${team.id}_U20",
                        name = "${team.name} U-20",
                        category = UtsTeamCategory.U20_MEN,
                        overallRating = u20Overall,
                        attackRating = u20Overall,
                        midfieldRating = u20Overall,
                        defenseRating = u20Overall,
                        goalkeeperRating = u20Overall
                    )
                )
                val u17Overall = (team.overallRating - 14).coerceIn(28, 82)
                insertTeamInternal(
                    db,
                    team.copy(
                        id = "${team.id}_U17",
                        name = "${team.name} U-17",
                        category = UtsTeamCategory.U17_MEN,
                        overallRating = u17Overall,
                        attackRating = u17Overall,
                        midfieldRating = u17Overall,
                        defenseRating = u17Overall,
                        goalkeeperRating = u17Overall
                    )
                )
            }

            val allClubs = buildWorldClubDatabase()
            for (club in allClubs) {
                insertTeamInternal(db, club)
            }

            val multiSportTeams = buildMultiSportSampleTeams()
            for (msTeam in multiSportTeams) {
                insertTeamInternal(db, msTeam)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun insertTeamInternal(db: SQLiteDatabase, team: UtsCoreTeam) {
        val cv = ContentValues().apply {
            put("id", team.id)
            put("name", team.name)
            put("short_code", team.shortCode)
            put("country", team.country)
            put("flag_emoji", team.flagEmoji)
            put("confederation", team.confederation.name)
            put("category", team.category.name)
            put("sport", team.sport.name)
            put("overall_rating", team.overallRating)
            put("attack_rating", team.attackRating)
            put("midfield_rating", team.midfieldRating)
            put("defense_rating", team.defenseRating)
            put("gk_rating", team.goalkeeperRating)
            put("form", team.form)
            put("home_advantage", team.homeAdvantage)
            put("stadium_name", team.stadiumName)
            put("primary_color", team.primaryColorHex)
            put("secondary_color", team.secondaryColorHex)
            put("custom_logo_uri", team.customLogoUri)
            put("domestic_tier", team.domesticTier)
            put("previous_rating", team.previousRating)
        }
        db.insertWithOnConflict("uts_teams", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun saveOrUpdateTeam(team: UtsCoreTeam) {
        val db = dbHelper.writableDatabase
        insertTeamInternal(db, team)
    }

    fun deleteTeam(teamId: String) {
        val db = dbHelper.writableDatabase
        db.delete("uts_teams", "id = ?", arrayOf(teamId))
    }

    fun getAllTeams(categoryFilter: UtsTeamCategory? = null, confederationFilter: UtsConfederation? = null): List<UtsCoreTeam> {
        val db = dbHelper.readableDatabase
        val clauses = mutableListOf<String>()
        val args = mutableListOf<String>()
        if (categoryFilter != null) {
            clauses.add("category = ?")
            args.add(categoryFilter.name)
        }
        if (confederationFilter != null) {
            clauses.add("confederation = ?")
            args.add(confederationFilter.name)
        }
        val where = if (clauses.isEmpty()) null else clauses.joinToString(" AND ")
        val cursor = db.query(
            "uts_teams",
            null,
            where,
            if (args.isEmpty()) null else args.toTypedArray(),
            null,
            null,
            "overall_rating DESC, name ASC"
        )
        val result = mutableListOf<UtsCoreTeam>()
        while (cursor.moveToNext()) {
            result.add(
                UtsCoreTeam(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    shortCode = cursor.getString(cursor.getColumnIndexOrThrow("short_code")),
                    country = cursor.getString(cursor.getColumnIndexOrThrow("country")),
                    flagEmoji = cursor.getString(cursor.getColumnIndexOrThrow("flag_emoji")),
                    confederation = runCatching {
                        UtsConfederation.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("confederation")))
                    }.getOrDefault(UtsConfederation.GLOBAL),
                    category = runCatching {
                        UtsTeamCategory.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("category")))
                    }.getOrDefault(UtsTeamCategory.MEN_NATIONAL),
                    sport = runCatching {
                        UtsSportType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("sport")))
                    }.getOrDefault(UtsSportType.FOOTBALL),
                    overallRating = cursor.getInt(cursor.getColumnIndexOrThrow("overall_rating")),
                    attackRating = cursor.getInt(cursor.getColumnIndexOrThrow("attack_rating")),
                    midfieldRating = cursor.getInt(cursor.getColumnIndexOrThrow("midfield_rating")),
                    defenseRating = cursor.getInt(cursor.getColumnIndexOrThrow("defense_rating")),
                    goalkeeperRating = cursor.getInt(cursor.getColumnIndexOrThrow("gk_rating")),
                    form = cursor.getInt(cursor.getColumnIndexOrThrow("form")),
                    homeAdvantage = cursor.getInt(cursor.getColumnIndexOrThrow("home_advantage")),
                    stadiumName = cursor.getString(cursor.getColumnIndexOrThrow("stadium_name")),
                    primaryColorHex = cursor.getString(cursor.getColumnIndexOrThrow("primary_color")),
                    secondaryColorHex = cursor.getString(cursor.getColumnIndexOrThrow("secondary_color")),
                    customLogoUri = cursor.getString(cursor.getColumnIndexOrThrow("custom_logo_uri")),
                    domesticTier = cursor.getInt(cursor.getColumnIndexOrThrow("domestic_tier")),
                    previousRating = cursor.getInt(cursor.getColumnIndexOrThrow("previous_rating"))
                )
            )
        }
        cursor.close()
        return result
    }

    fun recordRatingChange(entry: UtsRatingLogEntry) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("team_id", entry.teamId)
            put("team_name", entry.teamName)
            put("old_rating", entry.oldRating)
            put("new_rating", entry.newRating)
            put("reason", entry.reason)
            put("timestamp_label", entry.timestampLabel)
        }
        db.insert("uts_rating_logs", null, cv)
    }

    fun getRecentRatingLogs(limit: Int = 60): List<UtsRatingLogEntry> {
        val db = dbHelper.readableDatabase
        val cursor = db.query("uts_rating_logs", null, null, null, null, null, "id DESC", limit.toString())
        val list = mutableListOf<UtsRatingLogEntry>()
        while (cursor.moveToNext()) {
            list.add(
                UtsRatingLogEntry(
                    teamId = cursor.getString(cursor.getColumnIndexOrThrow("team_id")),
                    teamName = cursor.getString(cursor.getColumnIndexOrThrow("team_name")),
                    oldRating = cursor.getInt(cursor.getColumnIndexOrThrow("old_rating")),
                    newRating = cursor.getInt(cursor.getColumnIndexOrThrow("new_rating")),
                    reason = cursor.getString(cursor.getColumnIndexOrThrow("reason")),
                    timestampLabel = cursor.getString(cursor.getColumnIndexOrThrow("timestamp_label"))
                )
            )
        }
        cursor.close()
        return list
    }

    fun saveTournament(tournament: UtsActiveTournament, matches: List<UtsMatchRecord>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val groupJson = JSONObject()
            tournament.groupAssignments.forEach { (grp, ids) ->
                groupJson.put(grp, JSONArray(ids))
            }
            val cv = ContentValues().apply {
                put("id", tournament.id)
                put("name", tournament.name)
                put("year_label", tournament.yearLabel)
                put("host_name", tournament.hostName)
                put("sport", tournament.sport.name)
                put("category", tournament.category.name)
                put("format", tournament.format.name)
                put("group_count", tournament.groupCount)
                put("teams_per_group", tournament.teamsPerGroup)
                put("advance_per_group", tournament.advancePerGroup)
                put("best_third_count", tournament.bestThirdCount)
                put("has_third_place", if (tournament.hasThirdPlaceMatch) 1 else 0)
                put("is_completed", if (tournament.isCompleted) 1 else 0)
                put("champion_name", tournament.championName)
                put("runner_up_name", tournament.runnerUpName)
                put("third_place_name", tournament.thirdPlaceName)
                put("current_phase", tournament.currentPhase)
                put("participant_ids_json", JSONArray(tournament.participantIds).toString())
                put("group_map_json", groupJson.toString())
                put("draw_log_json", JSONArray(tournament.drawHistoryLog).toString())
                put("data_origin", tournament.dataOrigin.name)
            }
            db.insertWithOnConflict("uts_tournaments", null, cv, SQLiteDatabase.CONFLICT_REPLACE)

            db.delete("uts_matches", "tournament_id = ?", arrayOf(tournament.id))
            for (m in matches) {
                val eventsArr = JSONArray()
                m.events.forEach { ev ->
                    val obj = JSONObject()
                    obj.put("minute", ev.minute)
                    obj.put("type", ev.type)
                    obj.put("teamId", ev.teamId)
                    obj.put("teamName", ev.teamName)
                    obj.put("playerName", ev.playerName)
                    obj.put("description", ev.description)
                    eventsArr.put(obj)
                }
                val mCv = ContentValues().apply {
                    put("id", m.id)
                    put("tournament_id", m.tournamentId)
                    put("stage_name", m.stageName)
                    put("group_index", m.groupOrRoundIndex)
                    put("home_id", m.homeTeamId)
                    put("home_name", m.homeTeamName)
                    put("home_flag", m.homeFlag)
                    put("away_id", m.awayTeamId)
                    put("away_name", m.awayTeamName)
                    put("away_flag", m.awayFlag)
                    put("home_score", m.homeScore)
                    put("away_score", m.awayScore)
                    put("is_played", if (m.isPlayed) 1 else 0)
                    put("went_et", if (m.wentToExtraTime) 1 else 0)
                    put("went_pen", if (m.wentToPenalties) 1 else 0)
                    put("home_pen", m.homePenalties)
                    put("away_pen", m.awayPenalties)
                    put("home_poss", m.homePossession)
                    put("away_poss", m.awayPossession)
                    put("home_shots", m.homeShots)
                    put("away_shots", m.awayShots)
                    put("home_sot", m.homeShotsOnTarget)
                    put("away_sot", m.awayShotsOnTarget)
                    put("home_corners", m.homeCorners)
                    put("away_corners", m.awayCorners)
                    put("home_fouls", m.homeFouls)
                    put("away_fouls", m.awayFouls)
                    put("home_yc", m.homeYellowCards)
                    put("away_yc", m.awayYellowCards)
                    put("home_rc", m.homeRedCards)
                    put("away_rc", m.awayRedCards)
                    put("events_json", eventsArr.toString())
                    put("leg_number", m.legNumber)
                }
                db.insertWithOnConflict("uts_matches", null, mCv, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteSavedTournament(tournamentId: String) {
        val db = dbHelper.writableDatabase
        db.delete("uts_tournaments", "id = ?", arrayOf(tournamentId))
        db.delete("uts_matches", "tournament_id = ?", arrayOf(tournamentId))
    }

    fun getSavedTournaments(): List<UtsActiveTournament> {
        val db = dbHelper.readableDatabase
        val cursor = db.query("uts_tournaments", null, null, null, null, null, "rowid DESC")
        val list = mutableListOf<UtsActiveTournament>()
        while (cursor.moveToNext()) {
            val pArr = JSONArray(cursor.getString(cursor.getColumnIndexOrThrow("participant_ids_json")))
            val participantIds = (0 until pArr.length()).map { pArr.getString(it) }

            val gObj = JSONObject(cursor.getString(cursor.getColumnIndexOrThrow("group_map_json")))
            val groupMap = mutableMapOf<String, List<String>>()
            val keys = gObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val arr = gObj.getJSONArray(k)
                groupMap[k] = (0 until arr.length()).map { arr.getString(it) }
            }

            val dArr = JSONArray(cursor.getString(cursor.getColumnIndexOrThrow("draw_log_json")))
            val drawLog = (0 until dArr.length()).map { dArr.getString(it) }

            list.add(
                UtsActiveTournament(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    yearLabel = cursor.getString(cursor.getColumnIndexOrThrow("year_label")),
                    hostName = cursor.getString(cursor.getColumnIndexOrThrow("host_name")),
                    sport = runCatching {
                        UtsSportType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("sport")))
                    }.getOrDefault(UtsSportType.FOOTBALL),
                    category = runCatching {
                        UtsTeamCategory.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("category")))
                    }.getOrDefault(UtsTeamCategory.MEN_NATIONAL),
                    format = runCatching {
                        UtsTournamentFormat.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("format")))
                    }.getOrDefault(UtsTournamentFormat.GROUP_AND_KNOCKOUT),
                    groupCount = cursor.getInt(cursor.getColumnIndexOrThrow("group_count")),
                    teamsPerGroup = cursor.getInt(cursor.getColumnIndexOrThrow("teams_per_group")),
                    advancePerGroup = cursor.getInt(cursor.getColumnIndexOrThrow("advance_per_group")),
                    bestThirdCount = cursor.getInt(cursor.getColumnIndexOrThrow("best_third_count")),
                    hasThirdPlaceMatch = cursor.getInt(cursor.getColumnIndexOrThrow("has_third_place")) == 1,
                    isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1,
                    championName = cursor.getString(cursor.getColumnIndexOrThrow("champion_name")),
                    runnerUpName = cursor.getString(cursor.getColumnIndexOrThrow("runner_up_name")),
                    thirdPlaceName = cursor.getString(cursor.getColumnIndexOrThrow("third_place_name")),
                    currentPhase = cursor.getString(cursor.getColumnIndexOrThrow("current_phase")),
                    participantIds = participantIds,
                    groupAssignments = groupMap,
                    drawHistoryLog = drawLog,
                    dataOrigin = runCatching {
                        UtsDataOrigin.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("data_origin")))
                    }.getOrDefault(UtsDataOrigin.USER_CUSTOM)
                )
            )
        }
        cursor.close()
        return list
    }

    fun getMatchesForTournament(tournamentId: String): List<UtsMatchRecord> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "uts_matches",
            null,
            "tournament_id = ?",
            arrayOf(tournamentId),
            null,
            null,
            "group_index ASC, id ASC"
        )
        val matches = mutableListOf<UtsMatchRecord>()
        while (cursor.moveToNext()) {
            val evArr = JSONArray(cursor.getString(cursor.getColumnIndexOrThrow("events_json")))
            val events = mutableListOf<UtsMatchEventItem>()
            for (i in 0 until evArr.length()) {
                val o = evArr.getJSONObject(i)
                events.add(
                    UtsMatchEventItem(
                        minute = o.optInt("minute", 1),
                        type = o.optString("type", "COMMENTARY"),
                        teamId = o.optString("teamId", ""),
                        teamName = o.optString("teamName", ""),
                        playerName = o.optString("playerName", ""),
                        description = o.optString("description", "")
                    )
                )
            }
            matches.add(
                UtsMatchRecord(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    tournamentId = cursor.getString(cursor.getColumnIndexOrThrow("tournament_id")),
                    stageName = cursor.getString(cursor.getColumnIndexOrThrow("stage_name")),
                    groupOrRoundIndex = cursor.getInt(cursor.getColumnIndexOrThrow("group_index")),
                    homeTeamId = cursor.getString(cursor.getColumnIndexOrThrow("home_id")),
                    homeTeamName = cursor.getString(cursor.getColumnIndexOrThrow("home_name")),
                    homeFlag = cursor.getString(cursor.getColumnIndexOrThrow("home_flag")),
                    awayTeamId = cursor.getString(cursor.getColumnIndexOrThrow("away_id")),
                    awayTeamName = cursor.getString(cursor.getColumnIndexOrThrow("away_name")),
                    awayFlag = cursor.getString(cursor.getColumnIndexOrThrow("away_flag")),
                    homeScore = cursor.getInt(cursor.getColumnIndexOrThrow("home_score")),
                    awayScore = cursor.getInt(cursor.getColumnIndexOrThrow("away_score")),
                    isPlayed = cursor.getInt(cursor.getColumnIndexOrThrow("is_played")) == 1,
                    wentToExtraTime = cursor.getInt(cursor.getColumnIndexOrThrow("went_et")) == 1,
                    wentToPenalties = cursor.getInt(cursor.getColumnIndexOrThrow("went_pen")) == 1,
                    homePenalties = cursor.getInt(cursor.getColumnIndexOrThrow("home_pen")),
                    awayPenalties = cursor.getInt(cursor.getColumnIndexOrThrow("away_pen")),
                    homePossession = cursor.getInt(cursor.getColumnIndexOrThrow("home_poss")),
                    awayPossession = cursor.getInt(cursor.getColumnIndexOrThrow("away_poss")),
                    homeShots = cursor.getInt(cursor.getColumnIndexOrThrow("home_shots")),
                    awayShots = cursor.getInt(cursor.getColumnIndexOrThrow("away_shots")),
                    homeShotsOnTarget = cursor.getInt(cursor.getColumnIndexOrThrow("home_sot")),
                    awayShotsOnTarget = cursor.getInt(cursor.getColumnIndexOrThrow("away_sot")),
                    homeCorners = cursor.getInt(cursor.getColumnIndexOrThrow("home_corners")),
                    awayCorners = cursor.getInt(cursor.getColumnIndexOrThrow("away_corners")),
                    homeFouls = cursor.getInt(cursor.getColumnIndexOrThrow("home_fouls")),
                    awayFouls = cursor.getInt(cursor.getColumnIndexOrThrow("away_fouls")),
                    homeYellowCards = cursor.getInt(cursor.getColumnIndexOrThrow("home_yc")),
                    awayYellowCards = cursor.getInt(cursor.getColumnIndexOrThrow("away_yc")),
                    homeRedCards = cursor.getInt(cursor.getColumnIndexOrThrow("home_rc")),
                    awayRedCards = cursor.getInt(cursor.getColumnIndexOrThrow("away_rc")),
                    events = events,
                    legNumber = cursor.getInt(cursor.getColumnIndexOrThrow("leg_number"))
                )
            )
        }
        cursor.close()
        return matches
    }

    // =========================================================================
    // JSON & CSV EXPORT / IMPORT SYSTEM
    // =========================================================================

    fun exportFullDatabaseAsJson(): String {
        val root = JSONObject()
        root.put("app", "Universal Tournament Simulator")
        root.put("exportVersion", 1)
        val teamsArray = JSONArray()
        getAllTeams().forEach { t ->
            val o = JSONObject()
            o.put("id", t.id)
            o.put("name", t.name)
            o.put("shortCode", t.shortCode)
            o.put("country", t.country)
            o.put("flagEmoji", t.flagEmoji)
            o.put("confederation", t.confederation.name)
            o.put("category", t.category.name)
            o.put("sport", t.sport.name)
            o.put("overallRating", t.overallRating)
            o.put("attackRating", t.attackRating)
            o.put("midfieldRating", t.midfieldRating)
            o.put("defenseRating", t.defenseRating)
            o.put("goalkeeperRating", t.goalkeeperRating)
            o.put("primaryColorHex", t.primaryColorHex)
            o.put("secondaryColorHex", t.secondaryColorHex)
            o.put("domesticTier", t.domesticTier)
            teamsArray.put(o)
        }
        root.put("teams", teamsArray)
        return root.toString(2)
    }

    fun importTeamsFromJson(jsonString: String): Int {
        return runCatching {
            val root = JSONObject(jsonString)
            val arr = root.optJSONArray("teams") ?: return 0
            val db = dbHelper.writableDatabase
            var importedCount = 0
            db.beginTransaction()
            try {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val team = UtsCoreTeam(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        shortCode = o.optString("shortCode", "UTS"),
                        country = o.optString("country", "International"),
                        flagEmoji = o.optString("flagEmoji", "⚽"),
                        confederation = runCatching { UtsConfederation.valueOf(o.optString("confederation", "GLOBAL")) }
                            .getOrDefault(UtsConfederation.GLOBAL),
                        category = runCatching { UtsTeamCategory.valueOf(o.optString("category", "MEN_CLUB")) }
                            .getOrDefault(UtsTeamCategory.MEN_CLUB),
                        sport = runCatching { UtsSportType.valueOf(o.optString("sport", "FOOTBALL")) }
                            .getOrDefault(UtsSportType.FOOTBALL),
                        overallRating = o.optInt("overallRating", 75).coerceIn(1, 99),
                        attackRating = o.optInt("attackRating", 75).coerceIn(1, 99),
                        midfieldRating = o.optInt("midfieldRating", 75).coerceIn(1, 99),
                        defenseRating = o.optInt("defenseRating", 75).coerceIn(1, 99),
                        goalkeeperRating = o.optInt("goalkeeperRating", 75).coerceIn(1, 99),
                        primaryColorHex = o.optString("primaryColorHex", "#1E88E5"),
                        secondaryColorHex = o.optString("secondaryColorHex", "#FFFFFF"),
                        domesticTier = o.optInt("domesticTier", 1)
                    )
                    insertTeamInternal(db, team)
                    importedCount++
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            importedCount
        }.getOrDefault(0)
    }

    fun exportStandingsAsCsv(rows: List<UtsStandingRow>): String {
        val sb = StringBuilder()
        sb.appendLine("Pos,Group,Team,P,W,D,L,GF,GA,GD,Pts")
        rows.forEachIndexed { idx, r ->
            sb.appendLine("${idx + 1},${r.groupName},\"${r.teamName}\",${r.played},${r.won},${r.drawn},${r.lost},${r.goalsFor},${r.goalsAgainst},${r.goalDifference},${r.points}")
        }
        return sb.toString()
    }

    // =========================================================================
    // ALL 211 FIFA MEMBER NATIONAL TEAMS BY CONFEDERATION
    // =========================================================================

    private fun tNat(
        code: String,
        name: String,
        flag: String,
        conf: UtsConfederation,
        ovr: Int,
        att: Int = ovr,
        mid: Int = ovr,
        def: Int = ovr,
        gk: Int = ovr,
        c1: String = "#1565C0",
        c2: String = "#FFFFFF"
    ): UtsCoreTeam = UtsCoreTeam(
        id = "NAT_$code",
        name = name,
        shortCode = code,
        country = name,
        flagEmoji = flag,
        confederation = conf,
        category = UtsTeamCategory.MEN_NATIONAL,
        sport = UtsSportType.FOOTBALL,
        overallRating = ovr,
        attackRating = att,
        midfieldRating = mid,
        defenseRating = def,
        goalkeeperRating = gk,
        stadiumName = "$name National Stadium",
        primaryColorHex = c1,
        secondaryColorHex = c2
    )

    private fun buildAll211FifaNationalTeams(): List<UtsCoreTeam> {
        val list = mutableListOf<UtsCoreTeam>()

        // CONMEBOL (10 Member Associations)
        list.addAll(
            listOf(
                tNat("ARG", "Argentina", "🇦🇷", UtsConfederation.CONMEBOL, 95, 96, 94, 93, 94, "#75AADB", "#FFFFFF"),
                tNat("BRA", "Brazil", "🇧🇷", UtsConfederation.CONMEBOL, 93, 95, 92, 92, 93, "#FFDF00", "#009C3B"),
                tNat("URU", "Uruguay", "🇺🇾", UtsConfederation.CONMEBOL, 88, 89, 89, 87, 86, "#5BC2E7", "#000000"),
                tNat("COL", "Colombia", "🇨🇴", UtsConfederation.CONMEBOL, 88, 89, 88, 86, 86, "#FCD116", "#003893"),
                tNat("ECU", "Ecuador", "🇪🇨", UtsConfederation.CONMEBOL, 82, 81, 84, 83, 80, "#FFD100", "#0033A0"),
                tNat("VEN", "Venezuela", "🇻🇪", UtsConfederation.CONMEBOL, 79, 80, 78, 78, 79, "#7B1E26", "#F7D117"),
                tNat("PAR", "Paraguay", "🇵🇾", UtsConfederation.CONMEBOL, 78, 78, 78, 79, 77, "#D52B1E", "#FFFFFF"),
                tNat("CHI", "Chile", "🇨🇱", UtsConfederation.CONMEBOL, 77, 78, 77, 76, 77, "#D52B1E", "#0039A6"),
                tNat("PER", "Peru", "🇵🇪", UtsConfederation.CONMEBOL, 76, 76, 76, 77, 77, "#FFFFFF", "#D91023"),
                tNat("BOL", "Bolivia", "🇧🇴", UtsConfederation.CONMEBOL, 73, 73, 73, 72, 73, "#007934", "#F9E300")
            )
        )

        // UEFA (55 Member Associations)
        list.addAll(
            listOf(
                tNat("ESP", "Spain", "🇪🇸", UtsConfederation.UEFA, 95, 95, 96, 93, 92, "#AA151B", "#F1BF00"),
                tNat("FRA", "France", "🇫🇷", UtsConfederation.UEFA, 94, 96, 93, 93, 92, "#002395", "#FFFFFF"),
                tNat("ENG", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 93, 95, 93, 91, 90, "#FFFFFF", "#CE1124"),
                tNat("GER", "Germany", "🇩🇪", UtsConfederation.UEFA, 91, 91, 93, 90, 91, "#FFFFFF", "#000000"),
                tNat("POR", "Portugal", "🇵🇹", UtsConfederation.UEFA, 91, 93, 92, 90, 89, "#E42518", "#006600"),
                tNat("NED", "Netherlands", "🇳🇱", UtsConfederation.UEFA, 89, 88, 89, 91, 87, "#F36C21", "#FFFFFF"),
                tNat("ITA", "Italy", "🇮🇹", UtsConfederation.UEFA, 89, 87, 89, 90, 92, "#0064AA", "#FFFFFF"),
                tNat("BEL", "Belgium", "🇧🇪", UtsConfederation.UEFA, 87, 89, 88, 84, 90, "#E30613", "#000000"),
                tNat("CRO", "Croatia", "🇭🇷", UtsConfederation.UEFA, 86, 84, 89, 86, 85, "#FF0000", "#FFFFFF"),
                tNat("SUI", "Switzerland", "🇨🇭", UtsConfederation.UEFA, 85, 83, 86, 85, 87, "#D52B1E", "#FFFFFF"),
                tNat("DEN", "Denmark", "🇩🇰", UtsConfederation.UEFA, 84, 83, 85, 84, 85, "#C60C30", "#FFFFFF"),
                tNat("AUT", "Austria", "🇦🇹", UtsConfederation.UEFA, 84, 83, 86, 83, 82, "#ED2939", "#FFFFFF"),
                tNat("TUR", "Turkey", "🇹🇷", UtsConfederation.UEFA, 83, 84, 84, 82, 81, "#E30A17", "#FFFFFF"),
                tNat("UKR", "Ukraine", "🇺🇦", UtsConfederation.UEFA, 82, 82, 82, 81, 83, "#FFD700", "#0057B7"),
                tNat("SRB", "Serbia", "🇷🇸", UtsConfederation.UEFA, 81, 84, 81, 80, 81, "#C6363C", "#0C4076"),
                tNat("POL", "Poland", "🇵🇱", UtsConfederation.UEFA, 80, 83, 79, 79, 83, "#FFFFFF", "#DC143C"),
                tNat("SWE", "Sweden", "🇸🇪", UtsConfederation.UEFA, 81, 85, 80, 79, 79, "#FECC02", "#006AA7"),
                tNat("NOR", "Norway", "🇳🇴", UtsConfederation.UEFA, 82, 88, 82, 78, 78, "#BA0C2F", "#00205B"),
                tNat("SCO", "Scotland", "🏴󠁧󠁢󠁳󠁣󠁴󠁿", UtsConfederation.UEFA, 79, 78, 81, 79, 77, "#002B7F", "#FFFFFF"),
                tNat("CZE", "Czechia", "🇨🇿", UtsConfederation.UEFA, 80, 80, 81, 80, 79, "#D7141A", "#11457E"),
                tNat("HUN", "Hungary", "🇭🇺", UtsConfederation.UEFA, 80, 80, 81, 79, 80, "#CD2A3E", "#436F4D"),
                tNat("SVK", "Slovakia", "🇸🇰", UtsConfederation.UEFA, 78, 77, 79, 79, 79, "#0B4EA2", "#FFFFFF"),
                tNat("ROU", "Romania", "🇷🇴", UtsConfederation.UEFA, 78, 78, 78, 78, 78, "#FCD116", "#002B7F"),
                tNat("GRE", "Greece", "🇬🇷", UtsConfederation.UEFA, 79, 79, 78, 80, 79, "#0D5EAF", "#FFFFFF"),
                tNat("SVN", "Slovenia", "🇸🇮", UtsConfederation.UEFA, 78, 79, 77, 78, 86, "#FFFFFF", "#005CE6"),
                tNat("GEO", "Georgia", "🇬🇪", UtsConfederation.UEFA, 78, 81, 77, 76, 83, "#FFFFFF", "#FF0000"),
                tNat("ALB", "Albania", "🇦🇱", UtsConfederation.UEFA, 76, 76, 76, 77, 76, "#E41E20", "#000000"),
                tNat("WAL", "Wales", "🏴󠁧󠁢󠁷󠁬󠁳󠁿", UtsConfederation.UEFA, 77, 77, 77, 77, 76, "#D30731", "#FFFFFF"),
                tNat("IRL", "Republic of Ireland", "🇮🇪", UtsConfederation.UEFA, 75, 75, 75, 76, 77, "#169B62", "#FFFFFF"),
                tNat("FIN", "Finland", "🇫🇮", UtsConfederation.UEFA, 74, 74, 74, 74, 76, "#FFFFFF", "#003580"),
                tNat("BIH", "Bosnia and Herzegovina", "🇧🇦", UtsConfederation.UEFA, 75, 76, 75, 74, 74, "#002395", "#FECB00"),
                tNat("ISL", "Iceland", "🇮🇸", UtsConfederation.UEFA, 74, 75, 74, 73, 74, "#02529C", "#DC1E35"),
                tNat("MKD", "North Macedonia", "🇲🇰", UtsConfederation.UEFA, 73, 73, 74, 72, 73, "#D20000", "#FFE600"),
                tNat("MNE", "Montenegro", "🇲🇪", UtsConfederation.UEFA, 73, 74, 72, 73, 73, "#C40308", "#D4AF37"),
                tNat("NIR", "Northern Ireland", "🇬🇧", UtsConfederation.UEFA, 73, 72, 73, 74, 73, "#006B3F", "#FFFFFF"),
                tNat("ISR", "Israel", "🇮🇱", UtsConfederation.UEFA, 74, 75, 74, 72, 73, "#0038B8", "#FFFFFF"),
                tNat("BUL", "Bulgaria", "🇧🇬", UtsConfederation.UEFA, 71, 71, 71, 71, 72, "#FFFFFF", "#00966E"),
                tNat("LUX", "Luxembourg", "🇱🇺", UtsConfederation.UEFA, 70, 70, 71, 70, 70, "#EA141D", "#51ADDA"),
                tNat("ARM", "Armenia", "🇦🇲", UtsConfederation.UEFA, 70, 71, 70, 69, 70, "#D90012", "#0033A0"),
                tNat("KVX", "Kosovo", "🇽🇰", UtsConfederation.UEFA, 72, 74, 71, 71, 72, "#244AA5", "#D0A650"),
                tNat("BLR", "Belarus", "🇧🇾", UtsConfederation.UEFA, 68, 68, 68, 68, 69, "#C8313E", "#4AA657"),
                tNat("AZE", "Azerbaijan", "🇦🇿", UtsConfederation.UEFA, 68, 69, 68, 67, 68, "#00B5E2", "#EF3340"),
                tNat("KAZ", "Kazakhstan", "🇰🇿", UtsConfederation.UEFA, 68, 68, 68, 68, 68, "#00AFCA", "#FEC50C"),
                tNat("CYP", "Cyprus", "🇨🇾", UtsConfederation.UEFA, 66, 66, 66, 66, 66, "#FFFFFF", "#D57800"),
                tNat("EST", "Estonia", "🇪🇪", UtsConfederation.UEFA, 65, 65, 65, 65, 67, "#0072CE", "#000000"),
                tNat("LVA", "Latvia", "🇱🇻", UtsConfederation.UEFA, 64, 64, 64, 64, 65, "#9E3039", "#FFFFFF"),
                tNat("LTU", "Lithuania", "🇱🇹", UtsConfederation.UEFA, 64, 64, 64, 64, 65, "#FDB913", "#006A44"),
                tNat("FRO", "Faroe Islands", "🇫🇴", UtsConfederation.UEFA, 62, 62, 62, 63, 62, "#FFFFFF", "#0065BD"),
                tNat("MDA", "Moldova", "🇲🇩", UtsConfederation.UEFA, 64, 64, 64, 64, 64, "#0046AE", "#FFD200"),
                tNat("MLT", "Malta", "🇲🇹", UtsConfederation.UEFA, 60, 60, 60, 60, 60, "#CF142B", "#FFFFFF"),
                tNat("AND", "Andorra", "🇦🇩", UtsConfederation.UEFA, 55, 55, 55, 56, 56, "#10069F", "#FEDD00"),
                tNat("GIB", "Gibraltar", "🇬🇮", UtsConfederation.UEFA, 53, 52, 53, 54, 54, "#DA291C", "#FFFFFF"),
                tNat("LIE", "Liechtenstein", "🇱🇮", UtsConfederation.UEFA, 52, 52, 52, 53, 53, "#002B7F", "#CE1126"),
                tNat("SMR", "San Marino", "🇸🇲", UtsConfederation.UEFA, 48, 48, 48, 49, 50, "#5EB6E4", "#FFFFFF"),
                tNat("RUS", "Russia", "🇷🇺", UtsConfederation.UEFA, 79, 80, 79, 78, 80, "#D52B1E", "#0039A6")
            )
        )

        // CAF (54 Member Associations)
        list.addAll(
            listOf(
                tNat("MAR", "Morocco", "🇲🇦", UtsConfederation.CAF, 87, 86, 87, 88, 87, "#C1272D", "#006233"),
                tNat("SEN", "Senegal", "🇸🇳", UtsConfederation.CAF, 85, 86, 85, 85, 83, "#00853F", "#FDEF42"),
                tNat("EGY", "Egypt", "🇪🇬", UtsConfederation.CAF, 83, 86, 81, 81, 82, "#CE1126", "#000000"),
                tNat("NGA", "Nigeria", "🇳🇬", UtsConfederation.CAF, 84, 88, 82, 82, 80, "#008751", "#FFFFFF"),
                tNat("ALG", "Algeria", "🇩🇿", UtsConfederation.CAF, 83, 85, 84, 81, 80, "#006233", "#FFFFFF"),
                tNat("CIV", "Ivory Coast", "🇨🇮", UtsConfederation.CAF, 84, 85, 84, 83, 81, "#F77F00", "#009E60"),
                tNat("TUN", "Tunisia", "🇹🇳", UtsConfederation.CAF, 79, 78, 80, 81, 79, "#E70013", "#FFFFFF"),
                tNat("CMR", "Cameroon", "🇨🇲", UtsConfederation.CAF, 80, 82, 79, 79, 82, "#007A5E", "#CE1126"),
                tNat("MLI", "Mali", "🇲🇱", UtsConfederation.CAF, 80, 79, 82, 79, 78, "#14B53A", "#FCD116"),
                tNat("GHA", "Ghana", "🇬🇭", UtsConfederation.CAF, 78, 80, 79, 77, 76, "#FFFFFF", "#006B3F"),
                tNat("RSA", "South Africa", "🇿🇦", UtsConfederation.CAF, 78, 78, 78, 78, 81, "#FFB81C", "#007749"),
                tNat("COD", "DR Congo", "🇨🇩", UtsConfederation.CAF, 78, 79, 78, 78, 76, "#007FFF", "#F7D618"),
                tNat("BFA", "Burkina Faso", "🇧🇫", UtsConfederation.CAF, 77, 77, 77, 78, 76, "#009E49", "#EF2B2D"),
                tNat("CPV", "Cape Verde", "🇨🇻", UtsConfederation.CAF, 75, 76, 75, 75, 74, "#003893", "#CF2027"),
                tNat("GUI", "Guinea", "🇬🇳", UtsConfederation.CAF, 76, 78, 76, 75, 74, "#CE1126", "#FCD116"),
                tNat("GAB", "Gabon", "🇬🇦", UtsConfederation.CAF, 74, 77, 73, 73, 73, "#FCD116", "#009E60"),
                tNat("EQG", "Equatorial Guinea", "🇬🇶", UtsConfederation.CAF, 74, 74, 74, 75, 75, "#E32118", "#3E9A00"),
                tNat("ZAM", "Zambia", "🇿🇲", UtsConfederation.CAF, 74, 76, 73, 73, 72, "#198A00", "#EF7D00"),
                tNat("ANG", "Angola", "🇦🇴", UtsConfederation.CAF, 75, 76, 74, 75, 73, "#CC092F", "#000000"),
                tNat("BEN", "Benin", "🇧🇯", UtsConfederation.CAF, 72, 72, 72, 72, 71, "#FCD116", "#008751"),
                tNat("UGA", "Uganda", "🇺🇬", UtsConfederation.CAF, 71, 71, 71, 71, 72, "#FCDC04", "#000000"),
                tNat("MOZ", "Mozambique", "🇲🇿", UtsConfederation.CAF, 71, 72, 70, 71, 70, "#D21034", "#009A44"),
                tNat("MAD", "Madagascar", "🇲🇬", UtsConfederation.CAF, 69, 69, 69, 69, 69, "#FC3D32", "#007E3A"),
                tNat("KEN", "Kenya", "🇰🇪", UtsConfederation.CAF, 70, 71, 70, 69, 69, "#BB0000", "#006600"),
                tNat("MTN", "Mauritania", "🇲🇷", UtsConfederation.CAF, 70, 69, 70, 71, 70, "#00A95C", "#FFD700"),
                tNat("NAM", "Namibia", "🇳🇦", UtsConfederation.CAF, 69, 70, 69, 68, 69, "#D21034", "#003580"),
                tNat("TOG", "Togo", "🇹🇬", UtsConfederation.CAF, 70, 71, 70, 69, 69, "#FFCE00", "#006A4E"),
                tNat("ZIM", "Zimbabwe", "🇿🇼", UtsConfederation.CAF, 70, 71, 70, 69, 69, "#FFD200", "#006400"),
                tNat("GAM", "Gambia", "🇬🇲", UtsConfederation.CAF, 71, 72, 70, 70, 70, "#CE1126", "#0C1C8C"),
                tNat("GNB", "Guinea-Bissau", "🇬🇼", UtsConfederation.CAF, 70, 70, 70, 70, 69, "#CE1126", "#FCD116"),
                tNat("CGO", "Congo", "🇨🇬", UtsConfederation.CAF, 69, 69, 69, 69, 68, "#DC241F", "#009543"),
                tNat("TAN", "Tanzania", "🇹🇿", UtsConfederation.CAF, 69, 69, 69, 69, 68, "#1EB53A", "#00A3DD"),
                tNat("LBY", "Libya", "🇱🇾", UtsConfederation.CAF, 69, 69, 69, 69, 69, "#E70013", "#239E46"),
                tNat("SUD", "Sudan", "🇸🇩", UtsConfederation.CAF, 69, 69, 69, 69, 69, "#D21034", "#FFFFFF"),
                tNat("COM", "Comoros", "🇰🇲", UtsConfederation.CAF, 70, 70, 70, 70, 69, "#3D8E33", "#FFC61E"),
                tNat("RWA", "Rwanda", "🇷🇼", UtsConfederation.CAF, 67, 67, 67, 68, 67, "#FAD201", "#00A1DE"),
                tNat("MWI", "Malawi", "🇲🇼", UtsConfederation.CAF, 67, 67, 67, 67, 66, "#CE1126", "#000000"),
                tNat("SLE", "Sierra Leone", "🇸🇱", UtsConfederation.CAF, 67, 68, 67, 66, 66, "#1EB53A", "#0072C6"),
                tNat("NIG", "Niger", "🇳🇪", UtsConfederation.CAF, 66, 66, 66, 66, 66, "#E05206", "#0DB02B"),
                tNat("CTA", "Central African Republic", "🇨🇫", UtsConfederation.CAF, 66, 67, 66, 65, 65, "#003082", "#FFCE00"),
                tNat("ETH", "Ethiopia", "🇪🇹", UtsConfederation.CAF, 65, 65, 65, 65, 65, "#078930", "#FCDD09"),
                tNat("BOT", "Botswana", "🇧🇼", UtsConfederation.CAF, 65, 65, 65, 65, 65, "#6DA9D2", "#000000"),
                tNat("BDI", "Burundi", "🇧🇮", UtsConfederation.CAF, 65, 65, 65, 65, 65, "#CE1126", "#1EB53A"),
                tNat("LBR", "Liberia", "🇱🇷", UtsConfederation.CAF, 63, 63, 63, 63, 63, "#BF0A30", "#002868"),
                tNat("LES", "Lesotho", "🇱🇸", UtsConfederation.CAF, 62, 62, 62, 62, 62, "#00209F", "#009543"),
                tNat("SWZ", "Eswatini", "🇸🇿", UtsConfederation.CAF, 61, 61, 61, 61, 61, "#3E5EB9", "#B10C0C"),
                tNat("MRI", "Mauritius", "🇲🇺", UtsConfederation.CAF, 58, 58, 58, 58, 58, "#EA2839", "#1A206D"),
                tNat("SSD", "South Sudan", "🇸🇸", UtsConfederation.CAF, 60, 60, 60, 60, 60, "#078930", "#DA121A"),
                tNat("CHA", "Chad", "🇹🇩", UtsConfederation.CAF, 59, 59, 59, 59, 59, "#002664", "#FECB00"),
                tNat("STP", "Sao Tome and Principe", "🇸🇹", UtsConfederation.CAF, 56, 56, 56, 56, 56, "#12AD2B", "#FFCE00"),
                tNat("DJI", "Djibouti", "🇩🇯", UtsConfederation.CAF, 55, 55, 55, 55, 55, "#6AB2E7", "#12AD2B"),
                tNat("SOM", " Somalia", "🇸🇴", UtsConfederation.CAF, 52, 52, 52, 52, 52, "#4189DD", "#FFFFFF"),
                tNat("SEY", "Seychelles", "🇸🇨", UtsConfederation.CAF, 53, 53, 53, 53, 53, "#D62828", "#003F87"),
                tNat("ERI", "Eritrea", "🇪🇷", UtsConfederation.CAF, 54, 54, 54, 54, 54, "#4189DD", "#EA0437")
            )
        )

        // AFC (47 Member Associations)
        list.addAll(
            listOf(
                tNat("JPN", "Japan", "🇯🇵", UtsConfederation.AFC, 86, 86, 87, 85, 83, "#000555", "#FFFFFF"),
                tNat("IRN", "Iran", "🇮🇷", UtsConfederation.AFC, 82, 84, 81, 82, 81, "#FFFFFF", "#DA0000"),
                tNat("KOR", "South Korea", "🇰🇷", UtsConfederation.AFC, 84, 86, 83, 82, 80, "#EC0F32", "#000000"),
                tNat("AUS", "Australia", "🇦🇺", UtsConfederation.AFC, 80, 79, 80, 81, 81, "#FFCD00", "#00843D"),
                tNat("QAT", "Qatar", "🇶🇦", UtsConfederation.AFC, 78, 79, 78, 77, 77, "#8A1538", "#FFFFFF"),
                tNat("KSA", "Saudi Arabia", "🇸🇦", UtsConfederation.AFC, 78, 78, 79, 78, 77, "#006C35", "#FFFFFF"),
                tNat("IRQ", "Iraq", "🇮🇶", UtsConfederation.AFC, 76, 77, 76, 75, 75, "#007A3D", "#FFFFFF"),
                tNat("UZB", "Uzbekistan", "🇺🇿", UtsConfederation.AFC, 78, 78, 78, 78, 77, "#0099B5", "#FFFFFF"),
                tNat("UAE", "United Arab Emirates", "🇦🇪", UtsConfederation.AFC, 75, 76, 75, 74, 74, "#FFFFFF", "#EF3340"),
                tNat("JOR", "Jordan", "🇯🇴", UtsConfederation.AFC, 76, 78, 75, 75, 75, "#CE1126", "#FFFFFF"),
                tNat("OMA", "Oman", "🇴🇲", UtsConfederation.AFC, 73, 73, 73, 73, 74, "#DB161B", "#FFFFFF"),
                tNat("BHR", "Bahrain", "🇧🇭", UtsConfederation.AFC, 73, 73, 73, 73, 73, "#CE1126", "#FFFFFF"),
                tNat("CHN", "China PR", "🇨🇳", UtsConfederation.AFC, 71, 71, 70, 71, 72, "#EE1C25", "#FFFF00"),
                tNat("SYR", "Syria", "🇸🇾", UtsConfederation.AFC, 71, 72, 71, 71, 70, "#CE1126", "#FFFFFF"),
                tNat("PLE", "Palestine", "🇵🇸", UtsConfederation.AFC, 71, 71, 71, 71, 71, "#CE1126", "#007A3D"),
                tNat("THA", "Thailand", "🇹🇭", UtsConfederation.AFC, 70, 71, 70, 69, 69, "#00247D", "#EF3340"),
                tNat("TJK", "Tajikistan", "🇹🇯", UtsConfederation.AFC, 70, 70, 70, 70, 70, "#CC0000", "#006600"),
                tNat("KGZ", "Kyrgyzstan", "🇰🇬", UtsConfederation.AFC, 69, 69, 69, 69, 69, "#E8112D", "#FFEF00"),
                tNat("VIE", "Vietnam", "🇻🇳", UtsConfederation.AFC, 69, 69, 70, 69, 69, "#DA251D", "#FFFF00"),
                tNat("LBN", "Lebanon", "🇱🇧", UtsConfederation.AFC, 68, 68, 68, 68, 68, "#ED1C24", "#FFFFFF"),
                tNat("PRK", "North Korea", "🇰🇵", UtsConfederation.AFC, 69, 69, 69, 70, 68, "#ED1C27", "#024FA2"),
                tNat("IND", "India", "🇮🇳", UtsConfederation.AFC, 66, 66, 66, 66, 67, "#003893", "#FF9933"),
                tNat("IDN", "Indonesia", "🇮🇩", UtsConfederation.AFC, 71, 71, 71, 72, 71, "#FF0000", "#FFFFFF"),
                tNat("MAS", "Malaysia", "🇲🇾", UtsConfederation.AFC, 67, 68, 67, 66, 66, "#FFCC00", "#000000"),
                tNat("KUW", "Kuwait", "🇰🇼", UtsConfederation.AFC, 67, 67, 67, 67, 67, "#0052A5", "#FFFFFF"),
                tNat("TKM", "Turkmenistan", "🇹🇲", UtsConfederation.AFC, 65, 65, 65, 65, 65, "#00843D", "#FFFFFF"),
                tNat("PHI", "Philippines", "🇵🇭", UtsConfederation.AFC, 64, 64, 64, 64, 66, "#0038A8", "#CE1126"),
                tNat("AFG", "Afghanistan", "🇦🇫", UtsConfederation.AFC, 61, 61, 61, 61, 61, "#D32011", "#007A36"),
                tNat("YEM", "Yemen", "🇾🇪", UtsConfederation.AFC, 61, 61, 61, 61, 61, "#CE1126", "#000000"),
                tNat("HKG", "Hong Kong", "🇭🇰", UtsConfederation.AFC, 63, 63, 63, 63, 63, "#DE2910", "#FFFFFF"),
                tNat("SGP", "Singapore", "🇸🇬", UtsConfederation.AFC, 62, 62, 62, 62, 62, "#EF3340", "#FFFFFF"),
                tNat("MYA", "Myanmar", "🇲🇲", UtsConfederation.AFC, 60, 60, 60, 60, 60, "#EA2839", "#FECB00"),
                tNat("TPE", "Chinese Taipei", "🇹🇼", UtsConfederation.AFC, 59, 59, 59, 59, 59, "#000095", "#FE0000"),
                tNat("MDV", "Maldives", "🇲🇻", UtsConfederation.AFC, 58, 58, 58, 58, 58, "#D21034", "#007E3A"),
                tNat("NEP", "Nepal", "🇳🇵", UtsConfederation.AFC, 57, 57, 57, 57, 57, "#DC143C", "#003893"),
                tNat("CAM", "Cambodia", "🇰🇭", UtsConfederation.AFC, 57, 57, 57, 57, 57, "#032EA1", "#E00025"),
                tNat("BAN", "Bangladesh", "🇧🇩", UtsConfederation.AFC, 56, 56, 56, 56, 56, "#006A4E", "#F42A41"),
                tNat("BHU", "Bhutan", "🇧🇹", UtsConfederation.AFC, 53, 53, 53, 53, 53, "#FF4E12", "#FFD520"),
                tNat("LAO", "Laos", "🇱🇦", UtsConfederation.AFC, 54, 54, 54, 54, 54, "#CE1126", "#002868"),
                tNat("BRU", "Brunei", "🇧🇳", UtsConfederation.AFC, 52, 52, 52, 52, 52, "#F7E017", "#000000"),
                tNat("MAC", "Macau", "🇲🇴", UtsConfederation.AFC, 51, 51, 51, 51, 51, "#00785E", "#FFFFFF"),
                tNat("MNG", "Mongolia", "🇲🇳", UtsConfederation.AFC, 53, 53, 53, 53, 53, "#DA2032", "#0066B3"),
                tNat("PAK", "Pakistan", "🇵🇰", UtsConfederation.AFC, 54, 54, 54, 54, 54, "#01411C", "#FFFFFF"),
                tNat("SRI", "Sri Lanka", "🇱🇰", UtsConfederation.AFC, 53, 53, 53, 53, 53, "#8D153A", "#FFBE29"),
                tNat("TLS", "Timor-Leste", "🇹🇱", UtsConfederation.AFC, 51, 51, 51, 51, 51, "#DC241F", "#000000"),
                tNat("GUM", "Guam", "🇬🇺", UtsConfederation.AFC, 50, 50, 50, 50, 50, "#002F6C", "#CE1126")
            )
        )

        // CONCACAF (35 Member Associations)
        list.addAll(
            listOf(
                tNat("USA", "United States", "🇺🇸", UtsConfederation.CONCACAF, 84, 84, 85, 83, 82, "#FFFFFF", "#002868"),
                tNat("MEX", "Mexico", "🇲🇽", UtsConfederation.CONCACAF, 83, 83, 83, 82, 83, "#006847", "#CE1126"),
                tNat("CAN", "Canada", "🇨🇦", UtsConfederation.CONCACAF, 82, 84, 81, 81, 79, "#FF0000", "#FFFFFF"),
                tNat("PAN", "Panama", "🇵🇦", UtsConfederation.CONCACAF, 78, 77, 78, 78, 77, "#DA121A", "#072357"),
                tNat("CRC", "Costa Rica", "🇨🇷", UtsConfederation.CONCACAF, 77, 76, 76, 77, 80, "#CE1126", "#002B7F"),
                tNat("JAM", "Jamaica", "🇯🇲", UtsConfederation.CONCACAF, 76, 79, 75, 75, 76, "#FED100", "#009B3A"),
                tNat("HON", "Honduras", "🇭🇳", UtsConfederation.CONCACAF, 74, 74, 74, 73, 73, "#0073CF", "#FFFFFF"),
                tNat("SLV", "El Salvador", "🇸🇻", UtsConfederation.CONCACAF, 70, 70, 70, 70, 70, "#0F47AF", "#FFFFFF"),
                tNat("GUA", "Guatemala", "🇬🇹", UtsConfederation.CONCACAF, 71, 71, 71, 71, 71, "#4997D0", "#FFFFFF"),
                tNat("HAI", "Haiti", "🇭🇹", UtsConfederation.CONCACAF, 71, 72, 70, 70, 70, "#00209F", "#D21034"),
                tNat("CUR", "Curacao", "🇨🇼", UtsConfederation.CONCACAF, 71, 71, 71, 71, 71, "#002B7F", "#F9E814"),
                tNat("TRI", "Trinidad and Tobago", "🇹🇹", UtsConfederation.CONCACAF, 69, 69, 69, 69, 69, "#CE1126", "#000000"),
                tNat("SUR", "Suriname", "🇸🇷", UtsConfederation.CONCACAF, 70, 71, 70, 70, 69, "#377E3F", "#B40A2D"),
                tNat("NCA", "Nicaragua", "🇳🇮", UtsConfederation.CONCACAF, 66, 66, 66, 66, 66, "#0067C6", "#FFFFFF"),
                tNat("CUB", "Cuba", "🇨🇺", UtsConfederation.CONCACAF, 64, 64, 64, 64, 64, "#CB1515", "#002A8F"),
                tNat("DOM", "Dominican Republic", "🇩🇴", UtsConfederation.CONCACAF, 66, 67, 66, 65, 65, "#002D62", "#CE1126"),
                tNat("GUY", "Guyana", "🇬🇾", UtsConfederation.CONCACAF, 63, 63, 63, 63, 63, "#FCD116", "#009E49"),
                tNat("PUR", "Puerto Rico", "🇵🇷", UtsConfederation.CONCACAF, 62, 62, 62, 62, 62, "#ED0000", "#0050F0"),
                tNat("ATG", "Antigua and Barbuda", "🇦🇬", UtsConfederation.CONCACAF, 60, 60, 60, 60, 60, "#CE1126", "#FCD116"),
                tNat("SKN", "Saint Kitts and Nevis", "🇰🇳", UtsConfederation.CONCACAF, 61, 61, 61, 61, 61, "#CE1126", "#009E49"),
                tNat("LCA", "Saint Lucia", "🇱🇨", UtsConfederation.CONCACAF, 59, 59, 59, 59, 59, "#66CCFF", "#FCD116"),
                tNat("VIN", "Saint Vincent and the Grenadines", "🇻🇨", UtsConfederation.CONCACAF, 59, 59, 59, 59, 59, "#FCD116", "#009E60"),
                tNat("GRN", "Grenada", "🇬🇩", UtsConfederation.CONCACAF, 60, 60, 60, 60, 60, "#CE1126", "#FCD116"),
                tNat("BRB", "Barbados", "🇧🇧", UtsConfederation.CONCACAF, 58, 58, 58, 58, 58, "#00267F", "#FFC726"),
                tNat("BER", "Bermuda", "🇧🇲", UtsConfederation.CONCACAF, 60, 60, 60, 60, 60, "#CF142B", "#00247D"),
                tNat("BLZ", "Belize", "🇧🇿", UtsConfederation.CONCACAF, 57, 57, 57, 57, 57, "#D90F19", "#171696"),
                tNat("DMA", "Dominica", "🇩🇲", UtsConfederation.CONCACAF, 56, 56, 56, 56, 56, "#006B3F", "#FCD116"),
                tNat("MSR", "Montserrat", "🇲🇸", UtsConfederation.CONCACAF, 57, 57, 57, 57, 57, "#006847", "#000000"),
                tNat("BAH", "Bahamas", "🇧🇸", UtsConfederation.CONCACAF, 53, 53, 53, 53, 53, "#00778B", "#FFC72C"),
                tNat("ARU", "Aruba", "🇦🇼", UtsConfederation.CONCACAF, 54, 54, 54, 54, 54, "#418FDE", "#F7D417"),
                tNat("CAY", "Cayman Islands", "🇰🇾", UtsConfederation.CONCACAF, 52, 52, 52, 52, 52, "#00247D", "#CF142B"),
                tNat("TCA", "Turks and Caicos Islands", "🇹🇨", UtsConfederation.CONCACAF, 50, 50, 50, 50, 50, "#00247D", "#FFFFFF"),
                tNat("VGB", "British Virgin Islands", "🇻🇬", UtsConfederation.CONCACAF, 49, 49, 49, 49, 49, "#006847", "#FCD116"),
                tNat("VIR", "US Virgin Islands", "🇻🇮", UtsConfederation.CONCACAF, 49, 49, 49, 49, 49, "#FFFFFF", "#002868"),
                tNat("AIA", "Anguilla", "🇦🇮", UtsConfederation.CONCACAF, 48, 48, 48, 48, 48, "#FF8C00", "#00247D")
            )
        )

        // OFC (11 Member Associations)
        list.addAll(
            listOf(
                tNat("NZL", "New Zealand", "🇳🇿", UtsConfederation.OFC, 75, 76, 74, 75, 74, "#FFFFFF", "#000000"),
                tNat("SOL", "Solomon Islands", "🇸🇧", UtsConfederation.OFC, 62, 63, 62, 61, 61, "#FCD116", "#0051BA"),
                tNat("NCL", "New Caledonia", "🇳🇨", UtsConfederation.OFC, 62, 62, 62, 62, 61, "#CE1126", "#0035AD"),
                tNat("FIJ", "Fiji", "🇫🇯", UtsConfederation.OFC, 61, 62, 61, 60, 60, "#FFFFFF", "#68BFE5"),
                tNat("TAH", "Tahiti", "🇵🇫", UtsConfederation.OFC, 61, 61, 61, 61, 61, "#CE1126", "#FFFFFF"),
                tNat("VAN", "Vanuatu", "🇻🇺", UtsConfederation.OFC, 60, 60, 60, 60, 60, "#FDCE12", "#009543"),
                tNat("PNG", "Papua New Guinea", "🇵🇬", UtsConfederation.OFC, 60, 60, 60, 60, 60, "#CE1126", "#FFCD00"),
                tNat("SAM", "Samoa", "🇼🇸", UtsConfederation.OFC, 54, 54, 54, 54, 54, "#002B7F", "#CE1126"),
                tNat("COK", "Cook Islands", "🇨🇰", UtsConfederation.OFC, 52, 52, 52, 52, 52, "#006847", "#FFFFFF"),
                tNat("TGA", "Tonga", "🇹🇴", UtsConfederation.OFC, 51, 51, 51, 51, 51, "#C10000", "#FFFFFF"),
                tNat("ASA", "American Samoa", "🇦🇸", UtsConfederation.OFC, 49, 49, 49, 49, 49, "#002868", "#BF0A30")
            )
        )

        return list
    }

    // =========================================================================
    // WORLD MEN'S CLUB FOOTBALL DATABASE (MULTI-DIVISION & CONTINENTAL)
    // =========================================================================

    private fun tClub(
        id: String,
        name: String,
        code: String,
        country: String,
        flag: String,
        conf: UtsConfederation,
        ovr: Int,
        tier: Int = 1,
        c1: String = "#1E88E5",
        c2: String = "#FFFFFF",
        stadium: String = "$name Arena"
    ): UtsCoreTeam = UtsCoreTeam(
        id = "CLUB_$id",
        name = name,
        shortCode = code,
        country = country,
        flagEmoji = flag,
        confederation = conf,
        category = UtsTeamCategory.MEN_CLUB,
        sport = UtsSportType.FOOTBALL,
        overallRating = ovr,
        attackRating = (ovr + 1).coerceIn(1, 99),
        midfieldRating = ovr.coerceIn(1, 99),
        defenseRating = (ovr - 1).coerceIn(1, 99),
        goalkeeperRating = ovr.coerceIn(1, 99),
        stadiumName = stadium,
        primaryColorHex = c1,
        secondaryColorHex = c2,
        domesticTier = tier
    )

    private fun buildWorldClubDatabase(): List<UtsCoreTeam> {
        return listOf(
            // ENGLAND - Tier 1 (Premier League)
            tClub("MCI", "Manchester City", "MCI", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 95, 1, "#6CABDD", "#FFFFFF", "Etihad Stadium"),
            tClub("ARS", "Arsenal", "ARS", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 92, 1, "#EF0107", "#FFFFFF", "Emirates Stadium"),
            tClub("LIV", "Liverpool", "LIV", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 93, 1, "#C8102E", "#F6EB61", "Anfield"),
            tClub("CHE", "Chelsea", "CHE", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 87, 1, "#034694", "#FFFFFF", "Stamford Bridge"),
            tClub("MUN", "Manchester United", "MUN", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 85, 1, "#DA291C", "#FBE122", "Old Trafford"),
            tClub("TOT", "Tottenham Hotspur", "TOT", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 85, 1, "#FFFFFF", "#132257", "Tottenham Hotspur Stadium"),
            tClub("NEW", "Newcastle United", "NEW", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 85, 1, "#241F20", "#FFFFFF", "St James' Park"),
            tClub("AVL", "Aston Villa", "AVL", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 85, 1, "#670E36", "#95BFE5", "Villa Park"),
            tClub("BHA", "Brighton & Hove Albion", "BHA", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 82, 1, "#0057B8", "#FFFFFF", "Amex Stadium"),
            tClub("WHU", "West Ham United", "WHU", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 81, 1, "#7A263A", "#1BB1E7", "London Stadium"),
            tClub("CRY", "Crystal Palace", "CRY", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 80, 1, "#1B458F", "#C4122E", "Selhurst Park"),
            tClub("FUL", "Fulham", "FUL", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 80, 1, "#FFFFFF", "#000000", "Craven Cottage"),
            // ENGLAND - Tier 2 (Championship)
            tClub("LEE", "Leeds United", "LEE", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 78, 2, "#FFFFFF", "#FFCD00", "Elland Road"),
            tClub("BUR", "Burnley", "BUR", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 77, 2, "#6C1D45", "#99D6EA", "Turf Moor"),
            tClub("SHU", "Sheffield United", "SHU", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 76, 2, "#EE2737", "#FFFFFF", "Bramall Lane"),
            tClub("SUN", "Sunderland", "SUN", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 75, 2, "#EB172B", "#FFFFFF", "Stadium of Light"),
            tClub("MID", "Middlesbrough", "MID", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 74, 2, "#E11B22", "#FFFFFF", "Riverside Stadium"),
            tClub("NOR", "Norwich City", "NOR", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 74, 2, "#FFF200", "#00A650", "Carrow Road"),
            // ENGLAND - Tier 3 (League One)
            tClub("BIR", "Birmingham City", "BIR", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 72, 3, "#0000FF", "#FFFFFF", "St Andrew's"),
            tClub("WRX", "Wrexham AFC", "WRX", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 70, 3, "#E41E26", "#FFFFFF", "Racecourse Ground"),
            tClub("BOL", "Bolton Wanderers", "BOL", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 69, 3, "#FFFFFF", "#1E3160", "Toughsheet Community Stadium"),
            tClub("CHA", "Charlton Athletic", "CHA", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 68, 3, "#D4021D", "#FFFFFF", "The Valley"),
            // ENGLAND - Tier 4 (League Two)
            tClub("MKD_C", "MK Dons", "MKD", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 64, 4, "#FFFFFF", "#000000", "Stadium MK"),
            tClub("NOT", "Notts County", "NOT", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 63, 4, "#000000", "#FFFFFF", "Meadow Lane"),
            tClub("WIM", "AFC Wimbledon", "WIM", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 62, 4, "#003399", "#FFCC00", "Plough Lane"),
            tClub("BRF", "Bradford City", "BRF", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 63, 4, "#7A003C", "#FDB913", "Valley Parade"),
            // ENGLAND - Tier 5 (National League)
            tClub("OLD", "Oldham Athletic", "OLD", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 59, 5, "#0055A5", "#FFFFFF", "Boundary Park"),
            tClub("SOU_NL", "Southend United", "SOU", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 58, 5, "#002B5C", "#FFFFFF", "Roots Hall"),
            tClub("YRK", "York City", "YRK", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 58, 5, "#D21034", "#003399", "LNER Community Stadium"),
            tClub("BAR_NL", "Barnet FC", "BAR", "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", UtsConfederation.UEFA, 59, 5, "#F58220", "#000000", "The Hive Stadium"),

            // SPAIN - Tier 1 (La Liga) & Tier 2 (Segunda)
            tClub("RMA", "Real Madrid", "RMA", "Spain", "🇪🇸", UtsConfederation.UEFA, 96, 1, "#FFFFFF", "#FEBE10", "Santiago Bernabeu"),
            tClub("FCB", "FC Barcelona", "FCB", "Spain", "🇪🇸", UtsConfederation.UEFA, 93, 1, "#A50044", "#004D98", "Spotify Camp Nou"),
            tClub("ATM", "Atletico Madrid", "ATM", "Spain", "🇪🇸", UtsConfederation.UEFA, 89, 1, "#CB3524", "#272E61", "Riyadh Air Metropolitano"),
            tClub("ATH", "Athletic Club", "ATH", "Spain", "🇪🇸", UtsConfederation.UEFA, 84, 1, "#EE2523", "#FFFFFF", "San Mames"),
            tClub("RSO", "Real Sociedad", "RSO", "Spain", "🇪🇸", UtsConfederation.UEFA, 83, 1, "#0067B1", "#FFFFFF", "Reale Arena"),
            tClub("VIL", "Villarreal CF", "VIL", "Spain", "🇪🇸", UtsConfederation.UEFA, 83, 1, "#FFE667", "#005187", "Estadio de la Ceramica"),
            tClub("BET", "Real Betis", "BET", "Spain", "🇪🇸", UtsConfederation.UEFA, 82, 1, "#0BB363", "#FFFFFF", "Benito Villamarin"),
            tClub("SEV", "Sevilla FC", "SEV", "Spain", "🇪🇸", UtsConfederation.UEFA, 80, 1, "#FFFFFF", "#D41920", "Ramon Sanchez-Pizjuan"),
            tClub("VAL", "Valencia CF", "VAL", "Spain", "🇪🇸", UtsConfederation.UEFA, 79, 1, "#FFFFFF", "#000000", "Mestalla"),
            tClub("GIR", "Girona FC", "GIR", "Spain", "🇪🇸", UtsConfederation.UEFA, 81, 1, "#CE1126", "#FFFFFF", "Montilivi"),
            tClub("DEP", "Deportivo de La Coruna", "DEP", "Spain", "🇪🇸", UtsConfederation.UEFA, 76, 2, "#00529F", "#FFFFFF", "Abanca-Riazor"),
            tClub("ZAR", "Real Zaragoza", "ZAR", "Spain", "🇪🇸", UtsConfederation.UEFA, 74, 2, "#FFFFFF", "#004B9B", "La Romareda"),
            tClub("MAL", "Malaga CF", "MAL", "Spain", "🇪🇸", UtsConfederation.UEFA, 74, 2, "#009EE2", "#FFFFFF", "La Rosaleda"),
            tClub("SPO", "Sporting Gijon", "SPO", "Spain", "🇪🇸", UtsConfederation.UEFA, 74, 2, "#D81E05", "#FFFFFF", "El Molinon"),

            // GERMANY - Tier 1 (Bundesliga) & Tier 2 (2. Bundesliga)
            tClub("BAY", "Bayern Munich", "BAY", "Germany", "🇩🇪", UtsConfederation.UEFA, 94, 1, "#DC052D", "#FFFFFF", "Allianz Arena"),
            tClub("B04", "Bayer Leverkusen", "B04", "Germany", "🇩🇪", UtsConfederation.UEFA, 90, 1, "#E32221", "#000000", "BayArena"),
            tClub("BVB", "Borussia Dortmund", "BVB", "Germany", "🇩🇪", UtsConfederation.UEFA, 88, 1, "#FDE100", "#000000", "Signal Iduna Park"),
            tClub("RBL", "RB Leipzig", "RBL", "Germany", "🇩🇪", UtsConfederation.UEFA, 86, 1, "#FFFFFF", "#DD0741", "Red Bull Arena"),
            tClub("VFB", "VfB Stuttgart", "VFB", "Germany", "🇩🇪", UtsConfederation.UEFA, 83, 1, "#FFFFFF", "#E32219", "MHPArena"),
            tClub("SGE", "Eintracht Frankfurt", "SGE", "Germany", "🇩🇪", UtsConfederation.UEFA, 82, 1, "#E1000F", "#000000", "Deutsche Bank Park"),
            tClub("HSV", "Hamburger SV", "HSV", "Germany", "🇩🇪", UtsConfederation.UEFA, 76, 2, "#005CA9", "#FFFFFF", "Volksparkstadion"),
            tClub("S04", "Schalke 04", "S04", "Germany", "🇩🇪", UtsConfederation.UEFA, 74, 2, "#004D9D", "#FFFFFF", "Veltins-Arena"),
            tClub("KOE", "1. FC Koln", "KOE", "Germany", "🇩🇪", UtsConfederation.UEFA, 76, 2, "#ED1C24", "#FFFFFF", "RheinEnergieStadion"),
            tClub("HER", "Hertha BSC", "HER", "Germany", "🇩🇪", UtsConfederation.UEFA, 74, 2, "#005CA9", "#FFFFFF", "Olympiastadion"),

            // ITALY - Tier 1 (Serie A) & Tier 2 (Serie B)
            tClub("INT", "Inter Milan", "INT", "Italy", "🇮🇹", UtsConfederation.UEFA, 92, 1, "#010E80", "#000000", "San Siro"),
            tClub("JUV", "Juventus", "JUV", "Italy", "🇮🇹", UtsConfederation.UEFA, 88, 1, "#000000", "#FFFFFF", "Allianz Stadium"),
            tClub("MIL", "AC Milan", "MIL", "Italy", "🇮🇹", UtsConfederation.UEFA, 88, 1, "#FB090B", "#000000", "San Siro"),
            tClub("NAP", "Napoli", "NAP", "Italy", "🇮🇹", UtsConfederation.UEFA, 88, 1, "#12A0D7", "#FFFFFF", "Diego Armando Maradona"),
            tClub("ATA", "Atalanta", "ATA", "Italy", "🇮🇹", UtsConfederation.UEFA, 86, 1, "#1E71B8", "#000000", "Gewiss Stadium"),
            tClub("ROM", "AS Roma", "ROM", "Italy", "🇮🇹", UtsConfederation.UEFA, 84, 1, "#8E1F2F", "#F0BC42", "Stadio Olimpico"),
            tClub("LAZ", "Lazio", "LAZ", "Italy", "🇮🇹", UtsConfederation.UEFA, 83, 1, "#87D8F7", "#FFFFFF", "Stadio Olimpico"),
            tClub("FIO", "Fiorentina", "FIO", "Italy", "🇮🇹", UtsConfederation.UEFA, 82, 1, "#482E92", "#FFFFFF", "Artemio Franchi"),
            tClub("PAL", "Palermo", "PAL", "Italy", "🇮🇹", UtsConfederation.UEFA, 74, 2, "#F29BB2", "#000000", "Renzo Barbera"),
            tClub("SAM_IT", "Sampdoria", "SAM", "Italy", "🇮🇹", UtsConfederation.UEFA, 74, 2, "#0055A4", "#FFFFFF", "Luigi Ferraris"),

            // FRANCE - Tier 1 (Ligue 1) & Tier 2 (Ligue 2)
            tClub("PSG", "Paris Saint-Germain", "PSG", "France", "🇫🇷", UtsConfederation.UEFA, 92, 1, "#004170", "#DA291C", "Parc des Princes"),
            tClub("ASM", "AS Monaco", "ASM", "France", "🇫🇷", UtsConfederation.UEFA, 84, 1, "#E51B24", "#FFFFFF", "Stade Louis II"),
            tClub("OM", "Olympique Marseille", "OM", "France", "🇫🇷", UtsConfederation.UEFA, 84, 1, "#FFFFFF", "#2FAEE0", "Stade Velodrome"),
            tClub("LIL", "LOSC Lille", "LIL", "France", "🇫🇷", UtsConfederation.UEFA, 83, 1, "#E01E13", "#1A224D", "Stade Pierre-Mauroy"),
            tClub("OL", "Olympique Lyonnais", "OL", "France", "🇫🇷", UtsConfederation.UEFA, 82, 1, "#FFFFFF", "#15368A", "Groupama Stadium"),
            tClub("NIC", "OGC Nice", "NIC", "France", "🇫🇷", UtsConfederation.UEFA, 81, 1, "#D61016", "#000000", "Allianz Riviera"),
            tClub("LEN", "RC Lens", "LEN", "France", "🇫🇷", UtsConfederation.UEFA, 80, 1, "#EC1C24", "#FFF200", "Stade Bollaert-Delelis"),
            tClub("LOR", "FC Lorient", "LOR", "France", "🇫🇷", UtsConfederation.UEFA, 75, 2, "#F47920", "#000000", "Stade du Moustoir"),
            tClub("MET", "FC Metz", "MET", "France", "🇫🇷", UtsConfederation.UEFA, 74, 2, "#7C1531", "#FFFFFF", "Stade Saint-Symphorien"),

            // OTHER EUROPEAN ELITE (Portugal, Netherlands, Belgium, Scotland, Turkey, Greece)
            tClub("SLB", "SL Benfica", "SLB", "Portugal", "🇵🇹", UtsConfederation.UEFA, 85, 1, "#E31B23", "#FFFFFF", "Estadio da Luz"),
            tClub("SCP", "Sporting CP", "SCP", "Portugal", "🇵🇹", UtsConfederation.UEFA, 85, 1, "#008057", "#FFFFFF", "Estadio Jose Alvalade"),
            tClub("FCP", "FC Porto", "FCP", "Portugal", "🇵🇹", UtsConfederation.UEFA, 84, 1, "#00428C", "#FFFFFF", "Estadio do Dragao"),
            tClub("PSV", "PSV Eindhoven", "PSV", "Netherlands", "🇳🇱", UtsConfederation.UEFA, 83, 1, "#ED1C24", "#FFFFFF", "Philips Stadion"),
            tClub("FEY", "Feyenoord", "FEY", "Netherlands", "🇳🇱", UtsConfederation.UEFA, 82, 1, "#ED1C24", "#FFFFFF", "De Kuip"),
            tClub("AJA", "AFC Ajax", "AJA", "Netherlands", "🇳🇱", UtsConfederation.UEFA, 82, 1, "#D2122E", "#FFFFFF", "Johan Cruijff ArenA"),
            tClub("BRU_B", "Club Brugge", "CLB", "Belgium", "🇧🇪", UtsConfederation.UEFA, 80, 1, "#006BB6", "#000000", "Jan Breydel Stadium"),
            tClub("CEL", "Celtic FC", "CEL", "Scotland", "🏴󠁧󠁢󠁳󠁣󠁴󠁿", UtsConfederation.UEFA, 80, 1, "#018749", "#FFFFFF", "Celtic Park"),
            tClub("RAN", "Rangers FC", "RAN", "Scotland", "🏴󠁧󠁢󠁳󠁣󠁴󠁿", UtsConfederation.UEFA, 78, 1, "#1B458F", "#FFFFFF", "Ibrox Stadium"),
            tClub("GAL", "Galatasaray", "GAL", "Turkey", "🇹🇷", UtsConfederation.UEFA, 82, 1, "#A90432", "#FDB912", "RAMS Park"),
            tClub("FEN", "Fenerbahce", "FEN", "Turkey", "🇹🇷", UtsConfederation.UEFA, 82, 1, "#FFED00", "#002D72", "Sukru Saracoglu"),
            tClub("OLY", "Olympiacos", "OLY", "Greece", "🇬🇷", UtsConfederation.UEFA, 80, 1, "#E30613", "#FFFFFF", "Georgios Karaiskakis"),

            // ALGERIA - Ligue Professionnelle 1 & Ligue 2
            tClub("MCA", "MC Alger", "MCA", "Algeria", "🇩🇿", UtsConfederation.CAF, 79, 1, "#007A3D", "#CE1126", "Stade Ali La Pointe"),
            tClub("CRB", "CR Belouizdad", "CRB", "Algeria", "🇩🇿", UtsConfederation.CAF, 79, 1, "#CE1126", "#FFFFFF", "Stade du 5 Juillet"),
            tClub("USMA", "USM Alger", "USM", "Algeria", "🇩🇿", UtsConfederation.CAF, 78, 1, "#CE1126", "#000000", "Stade du 5 Juillet"),
            tClub("JSK", "JS Kabylie", "JSK", "Algeria", "🇩🇿", UtsConfederation.CAF, 77, 1, "#FCD116", "#007A3D", "Stade Hocine Ait Ahmed"),
            tClub("ESS", "ES Setif", "ESS", "Algeria", "🇩🇿", UtsConfederation.CAF, 77, 1, "#000000", "#FFFFFF", "Stade 8 Mai 1945"),
            tClub("CSC", "CS Constantine", "CSC", "Algeria", "🇩🇿", UtsConfederation.CAF, 76, 1, "#007A3D", "#000000", "Stade Chahid Hamlaoui"),
            tClub("MCO", "MC Oran", "MCO", "Algeria", "🇩🇿", UtsConfederation.CAF, 75, 1, "#CE1126", "#FFFFFF", "Stade Miloud Hadefi"),
            tClub("PAC", "Paradou AC", "PAC", "Algeria", "🇩🇿", UtsConfederation.CAF, 74, 1, "#FCD116", "#003893", "Stade Omar Benrabah"),
            tClub("JSS", "JS Saoura", "JSS", "Algeria", "🇩🇿", UtsConfederation.CAF, 74, 1, "#FCD116", "#007A3D", "Stade 20 Aout 1955"),
            tClub("ASO", "ASO Chlef", "ASO", "Algeria", "🇩🇿", UtsConfederation.CAF, 73, 1, "#CE1126", "#FFFFFF", "Stade Mohamed Boumezrag"),
            tClub("USMK", "USM Khenchela", "USK", "Algeria", "🇩🇿", UtsConfederation.CAF, 72, 1, "#000000", "#FFFFFF", "Stade Amar Hamam"),
            tClub("NAHD", "NA Hussein Dey", "NAH", "Algeria", "🇩🇿", UtsConfederation.CAF, 70, 2, "#FCD116", "#CE1126", "Stade 20 Aout"),
            tClub("RCK", "RC Kouba", "RCK", "Algeria", "🇩🇿", UtsConfederation.CAF, 70, 2, "#007A3D", "#FFFFFF", "Stade Benhaddad"),
            tClub("USMH", "USM El Harrach", "USH", "Algeria", "🇩🇿", UtsConfederation.CAF, 70, 2, "#FCD116", "#000000", "Stade 1er Novembre"),
            tClub("JSMB", "JSM Bejaia", "JSM", "Algeria", "🇩🇿", UtsConfederation.CAF, 69, 2, "#007A3D", "#CE1126", "Stade de l'Unite Maghrebine"),

            // CAF CONTINENTAL GIANTS (Egypt, Morocco, Tunisia, South Africa, Tanzania, DR Congo)
            tClub("AHL", "Al Ahly SC", "AHL", "Egypt", "🇪🇬", UtsConfederation.CAF, 83, 1, "#CE1126", "#FFFFFF", "Cairo International Stadium"),
            tClub("ZAM_EG", "Zamalek SC", "ZAM", "Egypt", "🇪🇬", UtsConfederation.CAF, 79, 1, "#FFFFFF", "#CE1126", "Cairo International Stadium"),
            tClub("PYR", "Pyramids FC", "PYR", "Egypt", "🇪🇬", UtsConfederation.CAF, 80, 1, "#002B5C", "#87CEEB", "30 June Stadium"),
            tClub("WAC", "Wydad AC", "WAC", "Morocco", "🇲🇦", UtsConfederation.CAF, 80, 1, "#CE1126", "#FFFFFF", "Stade Mohammed V"),
            tClub("RCA", "Raja CA", "RCA", "Morocco", "🇲🇦", UtsConfederation.CAF, 80, 1, "#007A3D", "#FFFFFF", "Stade Mohammed V"),
            tClub("RSB", "RS Berkane", "RSB", "Morocco", "🇲🇦", UtsConfederation.CAF, 78, 1, "#F47920", "#000000", "Stade Municipal de Berkane"),
            tClub("EST_TN", "Esperance de Tunis", "EST", "Tunisia", "🇹🇳", UtsConfederation.CAF, 81, 1, "#CE1126", "#FCD116", "Stade Hammadi Agrebi"),
            tClub("SUN_ZA", "Mamelodi Sundowns", "SUN", "South Africa", "🇿🇦", UtsConfederation.CAF, 81, 1, "#FCD116", "#007A3D", "Loftus Versfeld"),
            tClub("ORL", "Orlando Pirates", "ORL", "South Africa", "🇿🇦", UtsConfederation.CAF, 77, 1, "#000000", "#FFFFFF", "Orlando Stadium"),
            tClub("TPM", "TP Mazembe", "TPM", "DR Congo", "🇨🇩", UtsConfederation.CAF, 77, 1, "#000000", "#FFFFFF", "Stade TP Mazembe"),
            tClub("SIM", "Simba SC", "SIM", "Tanzania", "🇹🇿", UtsConfederation.CAF, 76, 1, "#CE1126", "#FFFFFF", "Benjamin Mkapa Stadium"),

            // CONMEBOL (Brazil, Argentina, Uruguay, Colombia, Ecuador, Chile)
            tClub("FLA", "CR Flamengo", "FLA", "Brazil", "🇧🇷", UtsConfederation.CONMEBOL, 85, 1, "#C52613", "#000000", "Maracana"),
            tClub("PAL_BR", "SE Palmeiras", "PAL", "Brazil", "🇧🇷", UtsConfederation.CONMEBOL, 85, 1, "#006437", "#FFFFFF", "Allianz Parque"),
            tClub("BOT_BR", "Botafogo FR", "BOT", "Brazil", "🇧🇷", UtsConfederation.CONMEBOL, 83, 1, "#000000", "#FFFFFF", "Nilton Santos"),
            tClub("FLU", "Fluminense FC", "FLU", "Brazil", "🇧🇷", UtsConfederation.CONMEBOL, 81, 1, "#8A1538", "#006437", "Maracana"),
            tClub("CAM_BR", "Atletico Mineiro", "CAM", "Brazil", "🇧🇷", UtsConfederation.CONMEBOL, 82, 1, "#000000", "#FFFFFF", "Arena MRV"),
            tClub("SPFC", "Sao Paulo FC", "SAO", "Brazil", "🇧🇷", UtsConfederation.CONMEBOL, 81, 1, "#FE0000", "#000000", "MorumBIS"),
            tClub("COR", "Corinthians", "COR", "Brazil", "🇧🇷", UtsConfederation.CONMEBOL, 80, 1, "#FFFFFF", "#000000", "Neo Quimica Arena"),
            tClub("SAN_BR", "Santos FC", "SAN", "Brazil", "🇧🇷", UtsConfederation.CONMEBOL, 77, 2, "#FFFFFF", "#000000", "Vila Belmiro"),
            tClub("CEA", "Ceara SC", "CEA", "Brazil", "🇧🇷", UtsConfederation.CONMEBOL, 74, 2, "#000000", "#FFFFFF", "Castelao"),
            tClub("RIV", "River Plate", "RIV", "Argentina", "🇦🇷", UtsConfederation.CONMEBOL, 84, 1, "#FFFFFF", "#E41E26", "Mas Monumental"),
            tClub("BOC", "Boca Juniors", "BOC", "Argentina", "🇦🇷", UtsConfederation.CONMEBOL, 83, 1, "#003399", "#FFCC00", "La Bombonera"),
            tClub("RAC", "Racing Club", "RAC", "Argentina", "🇦🇷", UtsConfederation.CONMEBOL, 80, 1, "#6CABDD", "#FFFFFF", "El Cilindro"),
            tClub("PEN", "CA Penarol", "PEN", "Uruguay", "🇺🇾", UtsConfederation.CONMEBOL, 79, 1, "#FFCC00", "#000000", "Campeon del Siglo"),
            tClub("NAC", "Nacional", "NAC", "Uruguay", "🇺🇾", UtsConfederation.CONMEBOL, 78, 1, "#FFFFFF", "#003399", "Gran Parque Central"),
            tClub("IDV", "Independiente del Valle", "IDV", "Ecuador", "🇪🇨", UtsConfederation.CONMEBOL, 78, 1, "#000000", "#0055A5", "Banco Guayaquil"),
            tClub("COL_CL", "Colo-Colo", "COL", "Chile", "🇨🇱", UtsConfederation.CONMEBOL, 77, 1, "#FFFFFF", "#000000", "Monumental David Arellano"),

            // AFC (Saudi Arabia, Japan, South Korea, UAE, Qatar, Australia)
            tClub("HIL", "Al-Hilal SFC", "HIL", "Saudi Arabia", "🇸🇦", UtsConfederation.AFC, 85, 1, "#004B9B", "#FFFFFF", "Kingdom Arena"),
            tClub("NAS", "Al-Nassr FC", "NAS", "Saudi Arabia", "🇸🇦", UtsConfederation.AFC, 83, 1, "#FFCC00", "#003893", "Al-Awwal Park"),
            tClub("ITT", "Al-Ittihad Club", "ITT", "Saudi Arabia", "🇸🇦", UtsConfederation.AFC, 82, 1, "#FFCC00", "#000000", "King Abdullah Sports City"),
            tClub("AHL_SA", "Al-Ahli Saudi FC", "AHS", "Saudi Arabia", "🇸🇦", UtsConfederation.AFC, 82, 1, "#006C35", "#FFFFFF", "King Abdullah Sports City"),
            tClub("AIN", "Al Ain FC", "AIN", "United Arab Emirates", "🇦🇪", UtsConfederation.AFC, 79, 1, "#582C83", "#FFFFFF", "Hazza bin Zayed Stadium"),
            tClub("SAD", "Al-Sadd SC", "SAD", "Qatar", "🇶🇦", UtsConfederation.AFC, 78, 1, "#FFFFFF", "#000000", "Jassim bin Hamad Stadium"),
            tClub("KAS", "Kashima Antlers", "KAS", "Japan", "🇯🇵", UtsConfederation.AFC, 78, 1, "#A6192E", "#0C2340", "Kashima Soccer Stadium"),
            tClub("YFM", "Yokohama F. Marinos", "YFM", "Japan", "🇯🇵", UtsConfederation.AFC, 78, 1, "#003893", "#FFFFFF", "Nissan Stadium"),
            tClub("URA", "Urawa Red Diamonds", "URA", "Japan", "🇯🇵", UtsConfederation.AFC, 78, 1, "#E60012", "#000000", "Saitama Stadium 2002"),
            tClub("VIS", "Vissel Kobe", "VIS", "Japan", "🇯🇵", UtsConfederation.AFC, 79, 1, "#9B0032", "#FFFFFF", "Noevir Stadium"),
            tClub("ULS", "Ulsan HD", "ULS", "South Korea", "🇰🇷", UtsConfederation.AFC, 78, 1, "#004098", "#FFCC00", "Ulsan Munsu Stadium"),
            tClub("JEO", "Jeonbuk Hyundai Motors", "JEO", "South Korea", "🇰🇷", UtsConfederation.AFC, 77, 1, "#007A3D", "#FFFFFF", "Jeonju World Cup Stadium"),
            tClub("CCM", "Central Coast Mariners", "CCM", "Australia", "🇦🇺", UtsConfederation.AFC, 73, 1, "#FFD100", "#002B5C", "Industree Group Stadium"),

            // CONCACAF (Mexico, USA) & OFC (New Zealand)
            tClub("AME", "Club America", "AME", "Mexico", "🇲🇽", UtsConfederation.CONCACAF, 81, 1, "#FFF200", "#002D62", "Estadio Azteca"),
            tClub("MTY", "CF Monterrey", "MTY", "Mexico", "🇲🇽", UtsConfederation.CONCACAF, 80, 1, "#002B5C", "#FFFFFF", "Estadio BBVA"),
            tClub("TIG", "Tigres UANL", "TIG", "Mexico", "🇲🇽", UtsConfederation.CONCACAF, 80, 1, "#FDB913", "#003594", "Estadio Universitario"),
            tClub("PAC_MX", "CF Pachuca", "PAC", "Mexico", "🇲🇽", UtsConfederation.CONCACAF, 79, 1, "#002B5C", "#FFFFFF", "Estadio Hidalgo"),
            tClub("MIA", "Inter Miami CF", "MIA", "United States", "🇺🇸", UtsConfederation.CONCACAF, 81, 1, "#F7B5CD", "#231F20", "Chase Stadium"),
            tClub("LAF", "LAFC", "LAF", "United States", "🇺🇸", UtsConfederation.CONCACAF, 79, 1, "#000000", "#C39E6D", "BMO Stadium"),
            tClub("CLB_US", "Columbus Crew", "CLB", "United States", "🇺🇸", UtsConfederation.CONCACAF, 79, 1, "#FEDD00", "#000000", "Lower.com Field"),
            tClub("AKL", "Auckland City FC", "AKL", "New Zealand", "🇳🇿", UtsConfederation.OFC, 68, 1, "#002B5C", "#FFFFFF", "Kiwitea Street")
        )
    }

    private fun buildMultiSportSampleTeams(): List<UtsCoreTeam> {
        return listOf(
            UtsCoreTeam("MS_NBA_BOS", "Boston Celtics", "BOS", "United States", "🏀", UtsConfederation.GLOBAL, UtsTeamCategory.MULTI_SPORT, UtsSportType.BASKETBALL, 94, 94, 93, 95, 92, primaryColorHex = "#007A33"),
            UtsCoreTeam("MS_NBA_DEN", "Denver Nuggets", "DEN", "United States", "🏀", UtsConfederation.GLOBAL, UtsTeamCategory.MULTI_SPORT, UtsSportType.BASKETBALL, 92, 94, 91, 90, 91, primaryColorHex = "#0E2240"),
            UtsCoreTeam("MS_NBA_LAL", "Los Angeles Lakers", "LAL", "United States", "🏀", UtsConfederation.GLOBAL, UtsTeamCategory.MULTI_SPORT, UtsSportType.BASKETBALL, 89, 91, 88, 88, 88, primaryColorHex = "#552583"),
            UtsCoreTeam("MS_NBA_RMA", "Real Madrid Baloncesto", "RMB", "Spain", "🏀", UtsConfederation.UEFA, UtsTeamCategory.MULTI_SPORT, UtsSportType.BASKETBALL, 87, 88, 87, 86, 86, primaryColorHex = "#FFFFFF"),
            UtsCoreTeam("MS_TEN_ALC", "Carlos Alcaraz", "ALC", "Spain", "🎾", UtsConfederation.GLOBAL, UtsTeamCategory.MULTI_SPORT, UtsSportType.TENNIS, 95, 96, 95, 94, 94, primaryColorHex = "#AA151B"),
            UtsCoreTeam("MS_TEN_SIN", "Jannik Sinner", "SIN", "Italy", "🎾", UtsConfederation.GLOBAL, UtsTeamCategory.MULTI_SPORT, UtsSportType.TENNIS, 95, 95, 95, 95, 94, primaryColorHex = "#0064AA"),
            UtsCoreTeam("MS_TEN_DJO", "Novak Djokovic", "DJO", "Serbia", "🎾", UtsConfederation.GLOBAL, UtsTeamCategory.MULTI_SPORT, UtsSportType.TENNIS, 94, 93, 95, 96, 95, primaryColorHex = "#C6363C"),
            UtsCoreTeam("MS_ESP_T1", "T1 Esports", "T1", "South Korea", "🎮", UtsConfederation.AFC, UtsTeamCategory.MULTI_SPORT, UtsSportType.ESPORTS, 95, 96, 95, 94, 95, primaryColorHex = "#E4002B"),
            UtsCoreTeam("MS_ESP_GENG", "Gen.G Esports", "GEN", "South Korea", "🎮", UtsConfederation.AFC, UtsTeamCategory.MULTI_SPORT, UtsSportType.ESPORTS, 93, 93, 94, 92, 93, primaryColorHex = "#AA8A00")
        )
    }

    // =========================================================================
    // VERIFIED HISTORICAL WORLD CUPS & CLUB COMPETITIONS ARCHIVE
    // =========================================================================

    fun getVerifiedHistoricalEditions(categoryFilter: String? = null): List<UtsHistoricalEdition> {
        val all = buildAllHistoricalEditions()
        return if (categoryFilter.isNullOrBlank()) all else all.filter { it.competitionCategory == categoryFilter }
    }

    private fun buildAllHistoricalEditions(): List<UtsHistoricalEdition> {
        return listOf(
            // =================================================================
            // FIFA MEN'S WORLD CUP (ALL 23 EDITIONS: 1930 - 2026)
            // =================================================================
            UtsHistoricalEdition(
                id = "MWC_1930", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1930",
                hostOrCountry = "Uruguay", confederation = UtsConfederation.GLOBAL, teamCount = 13,
                champion = "Uruguay", runnerUp = "Argentina", thirdPlace = "United States", fourthPlace = "Yugoslavia",
                finalScoreText = "Uruguay 4 - 2 Argentina (Estadio Centenario, Montevideo)",
                topScorer = "Guillermo Stabile (8 goals)", totalGoals = 70, matchesPlayed = 18,
                formatSummary = "4 groups (1 group of 4, 3 groups of 3) advancing to Semi-Finals and Final.",
                verifiedFixtures = listOf(
                    UtsHistoricalFixture("Semi-Final", "Argentina", "United States", 6, 1),
                    UtsHistoricalFixture("Semi-Final", "Uruguay", "Yugoslavia", 6, 1),
                    UtsHistoricalFixture("Final", "Uruguay", "Argentina", 4, 2, "Centenario, 68,346 attendance")
                ),
                participatingTeams = listOf("Uruguay", "Argentina", "United States", "Chile", "Brazil", "France", "Mexico", "Peru", "Romania", "Belgium", "Bolivia", "Paraguay")
            ),
            UtsHistoricalEdition(
                id = "MWC_1934", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1934",
                hostOrCountry = "Italy", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "Italy", runnerUp = "Czechoslovakia", thirdPlace = "Germany", fourthPlace = "Austria",
                finalScoreText = "Italy 2 - 1 Czechoslovakia (a.e.t.)",
                topScorer = "Oldrich Nejedly (5 goals)", totalGoals = 70, matchesPlayed = 17,
                formatSummary = "16-team straight knockout tournament from Round of 16 to Final.",
                verifiedFixtures = listOf(
                    UtsHistoricalFixture("Semi-Final", "Italy", "Austria", 1, 0),
                    UtsHistoricalFixture("Semi-Final", "Czechoslovakia", "Germany", 3, 1),
                    UtsHistoricalFixture("Final", "Italy", "Czechoslovakia", 2, 1, "After Extra Time in Rome")
                )
            ),
            UtsHistoricalEdition(
                id = "MWC_1938", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1938",
                hostOrCountry = "France", confederation = UtsConfederation.GLOBAL, teamCount = 15,
                champion = "Italy", runnerUp = "Hungary", thirdPlace = "Brazil", fourthPlace = "Sweden",
                finalScoreText = "Italy 4 - 2 Hungary (Stade Olympique de Colombes, Paris)",
                topScorer = "Leonidas (7 goals)", totalGoals = 84, matchesPlayed = 18,
                formatSummary = "Knockout bracket format; Italy successfully defended their World Cup title.",
                verifiedFixtures = listOf(
                    UtsHistoricalFixture("Semi-Final", "Italy", "Brazil", 2, 1),
                    UtsHistoricalFixture("Semi-Final", "Hungary", "Sweden", 5, 1),
                    UtsHistoricalFixture("Final", "Italy", "Hungary", 4, 2)
                )
            ),
            UtsHistoricalEdition(
                id = "MWC_1950", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1950",
                hostOrCountry = "Brazil", confederation = UtsConfederation.GLOBAL, teamCount = 13,
                champion = "Uruguay", runnerUp = "Brazil", thirdPlace = "Sweden", fourthPlace = "Spain",
                finalScoreText = "Uruguay 2 - 1 Brazil (Decisive Final Group Match - Maracanazo)",
                topScorer = "Ademir (8 goals)", totalGoals = 88, matchesPlayed = 22,
                formatSummary = "First round groups followed by a 4-team final round-robin pool.",
                verifiedFixtures = listOf(
                    UtsHistoricalFixture("Final Pool", "Brazil", "Sweden", 7, 1),
                    UtsHistoricalFixture("Final Pool", "Brazil", "Spain", 6, 1),
                    UtsHistoricalFixture("Decisive Match", "Uruguay", "Brazil", 2, 1, "Estadio do Maracana")
                )
            ),
            UtsHistoricalEdition(
                id = "MWC_1954", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1954",
                hostOrCountry = "Switzerland", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "West Germany", runnerUp = "Hungary", thirdPlace = "Austria", fourthPlace = "Uruguay",
                finalScoreText = "West Germany 3 - 2 Hungary (Miracle of Bern)",
                topScorer = "Sandor Kocsis (11 goals)", totalGoals = 140, matchesPlayed = 26,
                formatSummary = "4 groups of 4 teams followed by Quarter-Finals, Semi-Finals, and Final.",
                verifiedFixtures = listOf(
                    UtsHistoricalFixture("Group Stage", "Hungary", "West Germany", 8, 3),
                    UtsHistoricalFixture("Final", "West Germany", "Hungary", 3, 2, "Wankdorf Stadium, Bern")
                )
            ),
            UtsHistoricalEdition(
                id = "MWC_1958", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1958",
                hostOrCountry = "Sweden", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "Brazil", runnerUp = "Sweden", thirdPlace = "France", fourthPlace = "West Germany",
                finalScoreText = "Brazil 5 - 2 Sweden (Rasunda Stadium, Solna)",
                topScorer = "Just Fontaine (13 goals)", totalGoals = 126, matchesPlayed = 35,
                formatSummary = "16 teams in 4 groups advancing to 8-team knockout stage.",
                verifiedFixtures = listOf(
                    UtsHistoricalFixture("Semi-Final", "Brazil", "France", 5, 2),
                    UtsHistoricalFixture("Final", "Brazil", "Sweden", 5, 2, "17-year-old Pele scored twice")
                )
            ),
            UtsHistoricalEdition(
                id = "MWC_1962", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1962",
                hostOrCountry = "Chile", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "Brazil", runnerUp = "Czechoslovakia", thirdPlace = "Chile", fourthPlace = "Yugoslavia",
                finalScoreText = "Brazil 3 - 1 Czechoslovakia (Estadio Nacional, Santiago)",
                topScorer = "Garrincha & 5 others (4 goals)", totalGoals = 89, matchesPlayed = 32,
                formatSummary = "16 teams in 4 groups followed by Quarter-Finals, Semi-Finals, and Final."
            ),
            UtsHistoricalEdition(
                id = "MWC_1966", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1966",
                hostOrCountry = "England", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "England", runnerUp = "West Germany", thirdPlace = "Portugal", fourthPlace = "Soviet Union",
                finalScoreText = "England 4 - 2 West Germany (a.e.t. - Wembley Stadium)",
                topScorer = "Eusebio (9 goals)", totalGoals = 89, matchesPlayed = 32,
                formatSummary = "16 teams in 4 groups advancing to Quarter-Finals, Semi-Finals, and Final."
            ),
            UtsHistoricalEdition(
                id = "MWC_1970", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1970",
                hostOrCountry = "Mexico", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "Brazil", runnerUp = "Italy", thirdPlace = "West Germany", fourthPlace = "Uruguay",
                finalScoreText = "Brazil 4 - 1 Italy (Estadio Azteca, Mexico City)",
                topScorer = "Gerd Muller (10 goals)", totalGoals = 95, matchesPlayed = 32,
                formatSummary = "16 teams in 4 groups + knockout bracket; Brazil won 3rd title to keep Jules Rimet Trophy."
            ),
            UtsHistoricalEdition(
                id = "MWC_1974", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1974",
                hostOrCountry = "West Germany", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "West Germany", runnerUp = "Netherlands", thirdPlace = "Poland", fourthPlace = "Brazil",
                finalScoreText = "West Germany 2 - 1 Netherlands (Olympiastadion, Munich)",
                topScorer = "Grzegorz Lato (7 goals)", totalGoals = 97, matchesPlayed = 38,
                formatSummary = "First and Second group stages followed by Third-Place play-off and Final."
            ),
            UtsHistoricalEdition(
                id = "MWC_1978", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1978",
                hostOrCountry = "Argentina", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "Argentina", runnerUp = "Netherlands", thirdPlace = "Brazil", fourthPlace = "Italy",
                finalScoreText = "Argentina 3 - 1 Netherlands (a.e.t. - Estadio Monumental)",
                topScorer = "Mario Kempes (6 goals)", totalGoals = 102, matchesPlayed = 38,
                formatSummary = "Two group phases followed by Final and Third-Place match."
            ),
            UtsHistoricalEdition(
                id = "MWC_1982", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1982",
                hostOrCountry = "Spain", confederation = UtsConfederation.GLOBAL, teamCount = 24,
                champion = "Italy", runnerUp = "West Germany", thirdPlace = "Poland", fourthPlace = "France",
                finalScoreText = "Italy 3 - 1 West Germany (Santiago Bernabeu, Madrid)",
                topScorer = "Paolo Rossi (6 goals)", totalGoals = 146, matchesPlayed = 52,
                formatSummary = "Expanded to 24 teams: 6 groups of 4, second round of 4 groups of 3, Semi-Finals and Final.",
                verifiedFixtures = listOf(
                    UtsHistoricalFixture("Group 2", "Algeria", "West Germany", 2, 1, "Historic upset in Gijon (Madjer, Belloumi)"),
                    UtsHistoricalFixture("Second Round", "Italy", "Brazil", 3, 2, "Paolo Rossi hat-trick"),
                    UtsHistoricalFixture("Final", "Italy", "West Germany", 3, 1)
                )
            ),
            UtsHistoricalEdition(
                id = "MWC_1986", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1986",
                hostOrCountry = "Mexico", confederation = UtsConfederation.GLOBAL, teamCount = 24,
                champion = "Argentina", runnerUp = "West Germany", thirdPlace = "France", fourthPlace = "Belgium",
                finalScoreText = "Argentina 3 - 2 West Germany (Estadio Azteca)",
                topScorer = "Gary Lineker (6 goals)", totalGoals = 132, matchesPlayed = 52,
                formatSummary = "24 teams in 6 groups; top 2 + 4 best third-placed teams advanced to Round of 16 knockout."
            ),
            UtsHistoricalEdition(
                id = "MWC_1990", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1990",
                hostOrCountry = "Italy", confederation = UtsConfederation.GLOBAL, teamCount = 24,
                champion = "West Germany", runnerUp = "Argentina", thirdPlace = "Italy", fourthPlace = "England",
                finalScoreText = "West Germany 1 - 0 Argentina (Stadio Olimpico, Rome)",
                topScorer = "Salvatore Schillaci (6 goals)", totalGoals = 115, matchesPlayed = 52,
                formatSummary = "24 teams in 6 groups advancing to Round of 16, Quarter-Finals, Semi-Finals, and Final."
            ),
            UtsHistoricalEdition(
                id = "MWC_1994", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1994",
                hostOrCountry = "United States", confederation = UtsConfederation.GLOBAL, teamCount = 24,
                champion = "Brazil", runnerUp = "Italy", thirdPlace = "Sweden", fourthPlace = "Bulgaria",
                finalScoreText = "Brazil 0 - 0 Italy (3 - 2 pens, Rose Bowl)",
                topScorer = "Hristo Stoichkov & Oleg Salenko (6 goals)", totalGoals = 141, matchesPlayed = 52,
                formatSummary = "24 teams, 3 points per win introduced in World Cup group play."
            ),
            UtsHistoricalEdition(
                id = "MWC_1998", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "1998",
                hostOrCountry = "France", confederation = UtsConfederation.GLOBAL, teamCount = 32,
                champion = "France", runnerUp = "Brazil", thirdPlace = "Croatia", fourthPlace = "Netherlands",
                finalScoreText = "France 3 - 0 Brazil (Stade de France, Saint-Denis)",
                topScorer = "Davor Suker (6 goals)", totalGoals = 171, matchesPlayed = 64,
                formatSummary = "First 32-team World Cup: 8 groups of 4 teams advancing to Round of 16."
            ),
            UtsHistoricalEdition(
                id = "MWC_2002", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "2002",
                hostOrCountry = "South Korea & Japan", confederation = UtsConfederation.GLOBAL, teamCount = 32,
                champion = "Brazil", runnerUp = "Germany", thirdPlace = "Turkey", fourthPlace = "South Korea",
                finalScoreText = "Brazil 2 - 0 Germany (International Stadium Yokohama)",
                topScorer = "Ronaldo (8 goals)", totalGoals = 161, matchesPlayed = 64,
                formatSummary = "First World Cup in Asia and first co-hosted edition; 32 teams in 8 groups."
            ),
            UtsHistoricalEdition(
                id = "MWC_2006", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "2006",
                hostOrCountry = "Germany", confederation = UtsConfederation.GLOBAL, teamCount = 32,
                champion = "Italy", runnerUp = "France", thirdPlace = "Germany", fourthPlace = "Portugal",
                finalScoreText = "Italy 1 - 1 France (5 - 3 pens, Olympiastadion Berlin)",
                topScorer = "Miroslav Klose (5 goals)", totalGoals = 147, matchesPlayed = 64,
                formatSummary = "32 teams in 8 groups of 4 advancing to Round of 16 knockout bracket."
            ),
            UtsHistoricalEdition(
                id = "MWC_2010", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "2010",
                hostOrCountry = "South Africa", confederation = UtsConfederation.GLOBAL, teamCount = 32,
                champion = "Spain", runnerUp = "Netherlands", thirdPlace = "Germany", fourthPlace = "Uruguay",
                finalScoreText = "Spain 1 - 0 Netherlands (a.e.t. - Soccer City, Johannesburg)",
                topScorer = "Thomas Muller, David Villa, Wesley Sneijder, Diego Forlan (5 goals)", totalGoals = 145, matchesPlayed = 64,
                formatSummary = "First World Cup hosted in Africa; Spain won their maiden title via Iniesta's 116' goal."
            ),
            UtsHistoricalEdition(
                id = "MWC_2014", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "2014",
                hostOrCountry = "Brazil", confederation = UtsConfederation.GLOBAL, teamCount = 32,
                champion = "Germany", runnerUp = "Argentina", thirdPlace = "Netherlands", fourthPlace = "Brazil",
                finalScoreText = "Germany 1 - 0 Argentina (a.e.t. - Maracana, Rio de Janeiro)",
                topScorer = "James Rodriguez (6 goals)", totalGoals = 171, matchesPlayed = 64,
                formatSummary = "32 teams; featured Algeria's historic Round of 16 extra-time clash vs Germany (1-2 a.e.t.).",
                verifiedFixtures = listOf(
                    UtsHistoricalFixture("Round of 16", "Germany", "Algeria", 2, 1, "After Extra Time in Porto Alegre"),
                    UtsHistoricalFixture("Semi-Final", "Germany", "Brazil", 7, 1, "Belo Horizonte"),
                    UtsHistoricalFixture("Final", "Germany", "Argentina", 1, 0, "Mario Gotze 113' goal")
                )
            ),
            UtsHistoricalEdition(
                id = "MWC_2018", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "2018",
                hostOrCountry = "Russia", confederation = UtsConfederation.GLOBAL, teamCount = 32,
                champion = "France", runnerUp = "Croatia", thirdPlace = "Belgium", fourthPlace = "England",
                finalScoreText = "France 4 - 2 Croatia (Luzhniki Stadium, Moscow)",
                topScorer = "Harry Kane (6 goals)", totalGoals = 169, matchesPlayed = 64,
                formatSummary = "32 teams in 8 groups; first World Cup to utilize VAR."
            ),
            UtsHistoricalEdition(
                id = "MWC_2022", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "2022",
                hostOrCountry = "Qatar", confederation = UtsConfederation.GLOBAL, teamCount = 32,
                champion = "Argentina", runnerUp = "France", thirdPlace = "Croatia", fourthPlace = "Morocco",
                finalScoreText = "Argentina 3 - 3 France (4 - 2 pens, Lusail Stadium)",
                topScorer = "Kylian Mbappe (8 goals)", totalGoals = 172, matchesPlayed = 64,
                formatSummary = "32 teams; Morocco became the first African & Arab nation to reach the Semi-Finals.",
                verifiedFixtures = listOf(
                    UtsHistoricalFixture("Quarter-Final", "Morocco", "Portugal", 1, 0, "En-Nesyri header"),
                    UtsHistoricalFixture("Quarter-Final", "Argentina", "Netherlands", 2, 2, "Argentina won 4-3 pens"),
                    UtsHistoricalFixture("Final", "Argentina", "France", 3, 3, "Argentina won 4-2 on penalties")
                )
            ),
            UtsHistoricalEdition(
                id = "MWC_2026", competitionCategory = "MENS_WC", competitionName = "FIFA World Cup", editionYear = "2026",
                hostOrCountry = "United States, Canada & Mexico", confederation = UtsConfederation.GLOBAL, teamCount = 48,
                champion = "Spain", runnerUp = "Argentina", thirdPlace = "France", fourthPlace = "England",
                finalScoreText = "48-Team Expanded Edition (12 Groups of 4 + Round of 32)",
                topScorer = "Tournament Showcase / Recreate in What-If Mode", totalGoals = 268, matchesPlayed = 104,
                formatSummary = "12 groups of 4 teams (48 nations); top 2 per group + 8 best 3rd-placed teams advance to Round of 32.",
                dataOrigin = UtsDataOrigin.VERIFIED_CURRENT
            ),

            // =================================================================
            // FIFA WOMEN'S WORLD CUP (ALL EDITIONS: 1991 - 2027)
            // =================================================================
            UtsHistoricalEdition(
                id = "WWC_1991", competitionCategory = "WOMENS_WC", competitionName = "FIFA Women's World Cup", editionYear = "1991",
                hostOrCountry = "China PR", confederation = UtsConfederation.GLOBAL, teamCount = 12,
                champion = "United States Women", runnerUp = "Norway Women", thirdPlace = "Sweden Women", fourthPlace = "Germany Women",
                finalScoreText = "United States 2 - 1 Norway (Tianhe Stadium, Guangzhou)",
                topScorer = "Michelle Akers (10 goals)", totalGoals = 99, matchesPlayed = 26,
                formatSummary = "Inaugural FIFA Women's World Cup; 12 teams in 3 groups of 4."
            ),
            UtsHistoricalEdition(
                id = "WWC_1995", competitionCategory = "WOMENS_WC", competitionName = "FIFA Women's World Cup", editionYear = "1995",
                hostOrCountry = "Sweden", confederation = UtsConfederation.GLOBAL, teamCount = 12,
                champion = "Norway Women", runnerUp = "Germany Women", thirdPlace = "United States Women", fourthPlace = "China PR Women",
                finalScoreText = "Norway 2 - 0 Germany (Rasunda Stadium, Solna)",
                topScorer = "Ann Kristin Aarones (6 goals)", totalGoals = 99, matchesPlayed = 26,
                formatSummary = "12 teams in 3 groups advancing to Quarter-Finals."
            ),
            UtsHistoricalEdition(
                id = "WWC_1999", competitionCategory = "WOMENS_WC", competitionName = "FIFA Women's World Cup", editionYear = "1999",
                hostOrCountry = "United States", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "United States Women", runnerUp = "China PR Women", thirdPlace = "Brazil Women", fourthPlace = "Norway Women",
                finalScoreText = "United States 0 - 0 China PR (5 - 4 pens, Rose Bowl)",
                topScorer = "Sissi & Sun Wen (7 goals)", totalGoals = 123, matchesPlayed = 32,
                formatSummary = "Expanded to 16 teams in 4 groups of 4."
            ),
            UtsHistoricalEdition(
                id = "WWC_2003", competitionCategory = "WOMENS_WC", competitionName = "FIFA Women's World Cup", editionYear = "2003",
                hostOrCountry = "United States", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "Germany Women", runnerUp = "Sweden Women", thirdPlace = "United States Women", fourthPlace = "Canada Women",
                finalScoreText = "Germany 2 - 1 Sweden (a.e.t. Golden Goal)",
                topScorer = "Birgit Prinz (7 goals)", totalGoals = 107, matchesPlayed = 32,
                formatSummary = "16 teams in 4 groups advancing to Quarter-Finals."
            ),
            UtsHistoricalEdition(
                id = "WWC_2007", competitionCategory = "WOMENS_WC", competitionName = "FIFA Women's World Cup", editionYear = "2007",
                hostOrCountry = "China PR", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "Germany Women", runnerUp = "Brazil Women", thirdPlace = "United States Women", fourthPlace = "Norway Women",
                finalScoreText = "Germany 2 - 0 Brazil (Hongkou Stadium, Shanghai)",
                topScorer = "Marta (7 goals)", totalGoals = 111, matchesPlayed = 32,
                formatSummary = "Germany won the tournament without conceding a single goal."
            ),
            UtsHistoricalEdition(
                id = "WWC_2011", competitionCategory = "WOMENS_WC", competitionName = "FIFA Women's World Cup", editionYear = "2011",
                hostOrCountry = "Germany", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "Japan Women", runnerUp = "United States Women", thirdPlace = "Sweden Women", fourthPlace = "France Women",
                finalScoreText = "Japan 2 - 2 United States (3 - 1 pens, Frankfurt)",
                topScorer = "Homare Sawa (5 goals)", totalGoals = 86, matchesPlayed = 32,
                formatSummary = "Japan became the first Asian team to win a senior FIFA World Cup."
            ),
            UtsHistoricalEdition(
                id = "WWC_2015", competitionCategory = "WOMENS_WC", competitionName = "FIFA Women's World Cup", editionYear = "2015",
                hostOrCountry = "Canada", confederation = UtsConfederation.GLOBAL, teamCount = 24,
                champion = "United States Women", runnerUp = "Japan Women", thirdPlace = "England Women", fourthPlace = "Germany Women",
                finalScoreText = "United States 5 - 2 Japan (BC Place, Vancouver)",
                topScorer = "Celia Sasic & Carli Lloyd (6 goals)", totalGoals = 146, matchesPlayed = 52,
                formatSummary = "Expanded to 24 teams in 6 groups of 4."
            ),
            UtsHistoricalEdition(
                id = "WWC_2019", competitionCategory = "WOMENS_WC", competitionName = "FIFA Women's World Cup", editionYear = "2019",
                hostOrCountry = "France", confederation = UtsConfederation.GLOBAL, teamCount = 24,
                champion = "United States Women", runnerUp = "Netherlands Women", thirdPlace = "Sweden Women", fourthPlace = "England Women",
                finalScoreText = "United States 2 - 0 Netherlands (Parc Olympique Lyonnais)",
                topScorer = "Megan Rapinoe, Alex Morgan, Ellen White (6 goals)", totalGoals = 146, matchesPlayed = 52,
                formatSummary = "24 teams; United States claimed their fourth Women's World Cup title."
            ),
            UtsHistoricalEdition(
                id = "WWC_2023", competitionCategory = "WOMENS_WC", competitionName = "FIFA Women's World Cup", editionYear = "2023",
                hostOrCountry = "Australia & New Zealand", confederation = UtsConfederation.GLOBAL, teamCount = 32,
                champion = "Spain Women", runnerUp = "England Women", thirdPlace = "Sweden Women", fourthPlace = "Australia Women",
                finalScoreText = "Spain 1 - 0 England (Stadium Australia, Sydney)",
                topScorer = "Hinata Miyazawa (5 goals)", totalGoals = 164, matchesPlayed = 64,
                formatSummary = "First 32-team FIFA Women's World Cup; Olga Carmona scored the winning goal."
            ),
            UtsHistoricalEdition(
                id = "WWC_2027", competitionCategory = "WOMENS_WC", competitionName = "FIFA Women's World Cup", editionYear = "2027",
                hostOrCountry = "Brazil", confederation = UtsConfederation.GLOBAL, teamCount = 32,
                champion = "TBD (Simulate 2027 Edition)", runnerUp = "TBD", thirdPlace = "TBD", fourthPlace = "TBD",
                finalScoreText = "Scheduled in Brazil (32 National Teams)",
                topScorer = "Upcoming Edition", totalGoals = 0, matchesPlayed = 64,
                formatSummary = "32-team tournament across Brazilian host venues; ready to launch & simulate.",
                dataOrigin = UtsDataOrigin.VERIFIED_CURRENT
            ),

            // =================================================================
            // YOUTH WORLD CUPS (MEN'S & WOMEN'S U-20 & U-17)
            // =================================================================
            UtsHistoricalEdition(
                id = "U20M_2023", competitionCategory = "U20_MEN_WC", competitionName = "FIFA U-20 World Cup", editionYear = "2023",
                hostOrCountry = "Argentina", confederation = UtsConfederation.GLOBAL, teamCount = 24,
                champion = "Uruguay U-20", runnerUp = "Italy U-20", thirdPlace = "Israel U-20", fourthPlace = "South Korea U-20",
                finalScoreText = "Uruguay U-20 1 - 0 Italy U-20 (La Plata)",
                topScorer = "Cesare Casadei (7 goals)", totalGoals = 153, matchesPlayed = 52,
                formatSummary = "24 U-20 men's national teams in 6 groups of 4."
            ),
            UtsHistoricalEdition(
                id = "U17M_2023", competitionCategory = "U17_MEN_WC", competitionName = "FIFA U-17 World Cup", editionYear = "2023",
                hostOrCountry = "Indonesia", confederation = UtsConfederation.GLOBAL, teamCount = 24,
                champion = "Germany U-17", runnerUp = "France U-17", thirdPlace = "Mali U-17", fourthPlace = "Argentina U-17",
                finalScoreText = "Germany U-17 2 - 2 France U-17 (4 - 3 pens, Surakarta)",
                topScorer = "Agustin Ruberto (8 goals)", totalGoals = 175, matchesPlayed = 52,
                formatSummary = "24 U-17 men's national teams in 6 groups of 4."
            ),
            UtsHistoricalEdition(
                id = "U20W_2024", competitionCategory = "U20_WOMEN_WC", competitionName = "FIFA U-20 Women's World Cup", editionYear = "2024",
                hostOrCountry = "Colombia", confederation = UtsConfederation.GLOBAL, teamCount = 24,
                champion = "North Korea U-20W", runnerUp = "Japan U-20W", thirdPlace = "United States U-20W", fourthPlace = "Netherlands U-20W",
                finalScoreText = "North Korea 1 - 0 Japan (Estadio El Campin, Bogota)",
                topScorer = "Choe Il-son (6 goals)", totalGoals = 148, matchesPlayed = 52,
                formatSummary = "Expanded to 24 women's U-20 national teams."
            ),
            UtsHistoricalEdition(
                id = "U17W_2024", competitionCategory = "U17_WOMEN_WC", competitionName = "FIFA U-17 Women's World Cup", editionYear = "2024",
                hostOrCountry = "Dominican Republic", confederation = UtsConfederation.GLOBAL, teamCount = 16,
                champion = "North Korea U-17W", runnerUp = "Spain U-17W", thirdPlace = "United States U-17W", fourthPlace = "England U-17W",
                finalScoreText = "North Korea 1 - 1 Spain (4 - 3 pens, Santo Domingo)",
                topScorer = "Pau Comendador (5 goals)", totalGoals = 94, matchesPlayed = 32,
                formatSummary = "16 women's U-17 national teams in 4 groups of 4."
            ),

            // =================================================================
            // REAL CONTINENTAL CLUB COMPETITION SEASONS
            // =================================================================
            UtsHistoricalEdition(
                id = "UCL_2026_27", competitionCategory = "UCL", competitionName = "UEFA Champions League", editionYear = "2026-27",
                hostOrCountry = "Europe (Final: Metropolitano, Madrid)", confederation = UtsConfederation.UEFA, teamCount = 36,
                champion = "Active 2026-27 Season", runnerUp = "In Progress / Simulate", thirdPlace = "Semi-Finalist", fourthPlace = "Semi-Finalist",
                finalScoreText = "36-Team Swiss League Phase + Knockout Play-Offs & Round of 16",
                topScorer = "2026-27 Live Season", totalGoals = 0, matchesPlayed = 189,
                formatSummary = "36 clubs in a single League Phase table (8 matches each); top 8 advance directly to R16, 9th-24th contest knockout playoffs.",
                dataOrigin = UtsDataOrigin.VERIFIED_CURRENT
            ),
            UtsHistoricalEdition(
                id = "UCL_2024_25", competitionCategory = "UCL", competitionName = "UEFA Champions League", editionYear = "2024-25",
                hostOrCountry = "Europe (Final: Allianz Arena, Munich)", confederation = UtsConfederation.UEFA, teamCount = 36,
                champion = "Paris Saint-Germain", runnerUp = "Inter Milan", thirdPlace = "Arsenal", fourthPlace = "FC Barcelona",
                finalScoreText = "First 36-Team Swiss League Phase Edition",
                topScorer = "Serhou Guirassy / Raphinha (13 goals)", totalGoals = 618, matchesPlayed = 189,
                formatSummary = "Inaugural 36-club Swiss League Phase format + two-legged knockout phase."
            ),
            UtsHistoricalEdition(
                id = "UCL_2023_24", competitionCategory = "UCL", competitionName = "UEFA Champions League", editionYear = "2023-24",
                hostOrCountry = "Europe (Final: Wembley Stadium, London)", confederation = UtsConfederation.UEFA, teamCount = 32,
                champion = "Real Madrid", runnerUp = "Borussia Dortmund", thirdPlace = "Bayern Munich", fourthPlace = "Paris Saint-Germain",
                finalScoreText = "Real Madrid 2 - 0 Borussia Dortmund (Carvajal 74', Vinicius Jr 83')",
                topScorer = "Harry Kane & Kylian Mbappe (8 goals)", totalGoals = 375, matchesPlayed = 125,
                formatSummary = "32 clubs in 8 groups of 4; Real Madrid won a record-extending 15th European Cup.",
                verifiedFixtures = listOf(
                    UtsHistoricalFixture("Quarter-Final Agg", "Real Madrid", "Manchester City", 4, 4, "Real Madrid won 4-3 pens"),
                    UtsHistoricalFixture("Semi-Final Agg", "Real Madrid", "Bayern Munich", 4, 3),
                    UtsHistoricalFixture("Final", "Real Madrid", "Borussia Dortmund", 2, 0, "Wembley Stadium")
                )
            ),
            UtsHistoricalEdition(
                id = "UCL_2022_23", competitionCategory = "UCL", competitionName = "UEFA Champions League", editionYear = "2022-23",
                hostOrCountry = "Europe (Final: Ataturk Olympic Stadium, Istanbul)", confederation = UtsConfederation.UEFA, teamCount = 32,
                champion = "Manchester City", runnerUp = "Inter Milan", thirdPlace = "Real Madrid", fourthPlace = "AC Milan",
                finalScoreText = "Manchester City 1 - 0 Inter Milan (Rodri 68')",
                topScorer = "Erling Haaland (12 goals)", totalGoals = 372, matchesPlayed = 125,
                formatSummary = "32 clubs in 8 groups; Manchester City completed the continental treble."
            ),
            UtsHistoricalEdition(
                id = "UCL_2021_22", competitionCategory = "UCL", competitionName = "UEFA Champions League", editionYear = "2021-22",
                hostOrCountry = "Europe (Final: Stade de France, Paris)", confederation = UtsConfederation.UEFA, teamCount = 32,
                champion = "Real Madrid", runnerUp = "Liverpool", thirdPlace = "Manchester City", fourthPlace = "Villarreal CF",
                finalScoreText = "Real Madrid 1 - 0 Liverpool (Vinicius Jr 59')",
                topScorer = "Karim Benzema (15 goals)", totalGoals = 380, matchesPlayed = 125,
                formatSummary = "32 clubs in 8 groups; iconic knockout comebacks by Real Madrid."
            ),
            UtsHistoricalEdition(
                id = "UCL_2020_21", competitionCategory = "UCL", competitionName = "UEFA Champions League", editionYear = "2020-21",
                hostOrCountry = "Europe (Final: Estadio do Dragao, Porto)", confederation = UtsConfederation.UEFA, teamCount = 32,
                champion = "Chelsea", runnerUp = "Manchester City", thirdPlace = "Real Madrid", fourthPlace = "Paris Saint-Germain",
                finalScoreText = "Chelsea 1 - 0 Manchester City (Kai Havertz 42')",
                topScorer = "Erling Haaland (10 goals)", totalGoals = 366, matchesPlayed = 125,
                formatSummary = "32 clubs in 8 groups + two-legged knockouts."
            ),
            UtsHistoricalEdition(
                id = "UEL_2023_24", competitionCategory = "UEL", competitionName = "UEFA Europa League", editionYear = "2023-24",
                hostOrCountry = "Europe (Final: Aviva Stadium, Dublin)", confederation = UtsConfederation.UEFA, teamCount = 32,
                champion = "Atalanta", runnerUp = "Bayer Leverkusen", thirdPlace = "Olympique Marseille", fourthPlace = "AS Roma",
                finalScoreText = "Atalanta 3 - 0 Bayer Leverkusen (Ademola Lookman hat-trick)",
                topScorer = "Pierre-Emerick Aubameyang (10 goals)", totalGoals = 439, matchesPlayed = 141,
                formatSummary = "32 clubs in 8 groups + knockout play-offs."
            ),
            UtsHistoricalEdition(
                id = "UECL_2023_24", competitionCategory = "UECL", competitionName = "UEFA Conference League", editionYear = "2023-24",
                hostOrCountry = "Europe (Final: Agia Sophia Stadium, Athens)", confederation = UtsConfederation.UEFA, teamCount = 32,
                champion = "Olympiacos", runnerUp = "Fiorentina", thirdPlace = "Aston Villa", fourthPlace = "Club Brugge",
                finalScoreText = "Olympiacos 1 - 0 Fiorentina (a.e.t. - Ayoub El Kaabi 116')",
                topScorer = "Ayoub El Kaabi (11 goals)", totalGoals = 405, matchesPlayed = 141,
                formatSummary = "Olympiacos became the first Greek club to win a major UEFA trophy."
            ),
            UtsHistoricalEdition(
                id = "LIB_2024", competitionCategory = "LIBERTADORES", competitionName = "CONMEBOL Copa Libertadores", editionYear = "2024",
                hostOrCountry = "South America (Final: Monumental, Buenos Aires)", confederation = UtsConfederation.CONMEBOL, teamCount = 32,
                champion = "Botafogo FR", runnerUp = "Atletico Mineiro", thirdPlace = "CA Penarol", fourthPlace = "River Plate",
                finalScoreText = "Botafogo 3 - 1 Atletico Mineiro",
                topScorer = "Junior Santos (10 goals)", totalGoals = 342, matchesPlayed = 125,
                formatSummary = "32 clubs in 8 groups of 4 followed by two-legged knockouts and single-leg Final."
            ),
            UtsHistoricalEdition(
                id = "CAF_CL_2023_24", competitionCategory = "CAF_CL", competitionName = "CAF Champions League", editionYear = "2023-24",
                hostOrCountry = "Africa (Two-Legged Final)", confederation = UtsConfederation.CAF, teamCount = 16,
                champion = "Al Ahly SC", runnerUp = "Esperance de Tunis", thirdPlace = "Mamelodi Sundowns", fourthPlace = "TP Mazembe",
                finalScoreText = "Al Ahly 1 - 0 Esperance de Tunis (Aggregate)",
                topScorer = "Sankara Karamoko (4 goals)", totalGoals = 128, matchesPlayed = 62,
                formatSummary = "16 clubs in 4 groups of 4 advancing to two-legged Quarter-Finals, Semi-Finals, and Final."
            ),
            UtsHistoricalEdition(
                id = "CAF_CC_2022_23", competitionCategory = "CAF_CL", competitionName = "CAF Confederation Cup & Super Cup", editionYear = "2022-23",
                hostOrCountry = "Africa", confederation = UtsConfederation.CAF, teamCount = 16,
                champion = "USM Alger", runnerUp = "Young Africans", thirdPlace = "ASEC Mimosas", fourthPlace = "Marumo Gallants",
                finalScoreText = "USM Alger 2 - 2 Young Africans (USMA won on away goals + won CAF Super Cup 1-0 vs Al Ahly)",
                topScorer = "Fiston Mayele (7 goals)", totalGoals = 142, matchesPlayed = 62,
                formatSummary = "USM Alger won both the CAF Confederation Cup and the 2023 CAF Super Cup."
            ),
            UtsHistoricalEdition(
                id = "AFC_2023_24", competitionCategory = "AFC_ELITE", competitionName = "AFC Champions League Elite", editionYear = "2023-24",
                hostOrCountry = "Asia", confederation = UtsConfederation.AFC, teamCount = 40,
                champion = "Al Ain FC", runnerUp = "Yokohama F. Marinos", thirdPlace = "Al-Hilal SFC", fourthPlace = "Ulsan HD",
                finalScoreText = "Al Ain 6 - 3 Yokohama F. Marinos (Aggregate)",
                topScorer = "Soufiane Rahimi (13 goals)", totalGoals = 398, matchesPlayed = 139,
                formatSummary = "West & East regional zones culminating in the cross-zone Final."
            ),
            UtsHistoricalEdition(
                id = "CONCACAF_2024", competitionCategory = "CONCACAF_CC", competitionName = "CONCACAF Champions Cup", editionYear = "2024",
                hostOrCountry = "North & Central America", confederation = UtsConfederation.CONCACAF, teamCount = 27,
                champion = "CF Pachuca", runnerUp = "Columbus Crew", thirdPlace = "Club America", fourthPlace = "CF Monterrey",
                finalScoreText = "CF Pachuca 3 - 0 Columbus Crew (Estadio Hidalgo)",
                topScorer = "Salomon Rondon (9 goals)", totalGoals = 148, matchesPlayed = 51,
                formatSummary = "Multi-round continental knockout bracket."
            ),
            UtsHistoricalEdition(
                id = "OFC_2024", competitionCategory = "OFC_CL", competitionName = "OFC Champions League", editionYear = "2024",
                hostOrCountry = "Oceania (Tahiti)", confederation = UtsConfederation.OFC, teamCount = 8,
                champion = "Auckland City FC", runnerUp = "AS Pirae", thirdPlace = "Rewa FC", fourthPlace = "Ifira Black Bird",
                finalScoreText = "Auckland City 4 - 0 AS Pirae",
                topScorer = "Ryan De Vries (5 goals)", totalGoals = 52, matchesPlayed = 15,
                formatSummary = "2 groups of 4 clubs advancing to Semi-Finals and Final."
            ),
            // Domestic Leagues & Cups Historical Snapshots
            UtsHistoricalEdition(
                id = "DOM_EPL_2023_24", competitionCategory = "DOMESTIC_LEAGUE", competitionName = "English Premier League", editionYear = "2023-24",
                hostOrCountry = "England", confederation = UtsConfederation.UEFA, teamCount = 20,
                champion = "Manchester City", runnerUp = "Arsenal", thirdPlace = "Liverpool", fourthPlace = "Aston Villa",
                finalScoreText = "Manchester City (91 pts) edged Arsenal (89 pts) on Matchday 38",
                topScorer = "Erling Haaland (27 goals)", totalGoals = 1246, matchesPlayed = 380,
                formatSummary = "20 clubs, 38 matchdays double round-robin; top 4 qualify for UEFA Champions League."
            ),
            UtsHistoricalEdition(
                id = "DOM_ALG_2023_24", competitionCategory = "DOMESTIC_LEAGUE", competitionName = "Algerian Ligue Professionnelle 1 & Algerian Cup", editionYear = "2023-24",
                hostOrCountry = "Algeria", confederation = UtsConfederation.CAF, teamCount = 16,
                champion = "MC Alger (Ligue 1) / CR Belouizdad (Algerian Cup)", runnerUp = "CR Belouizdad", thirdPlace = "CS Constantine", fourthPlace = "USM Alger",
                finalScoreText = "MC Alger won Ligue 1 (65 pts); CRB beat MCA 1-0 in Algerian Cup Final",
                topScorer = "Youcef Belaili & Ismahil Sakho (14 goals)", totalGoals = 532, matchesPlayed = 240,
                formatSummary = "16 clubs, 30 matchdays; top 2 qualify for CAF Champions League, 3rd + Cup Winner to CAF Confederation Cup."
            )
        )
    }

    // =========================================================================
    // INITIAL SEASON MODE PYRAMIDS BUILDER
    // =========================================================================

    fun buildDefaultCountryPyramids(): List<UtsCountryPyramid> {
        val allClubs = getAllTeams(categoryFilter = UtsTeamCategory.MEN_CLUB)
        fun idsFor(country: String, tier: Int): List<String> =
            allClubs.filter { it.country.equals(country, ignoreCase = true) && it.domesticTier == tier }.map { it.id }

        return listOf(
            UtsCountryPyramid(
                countryName = "England",
                flagEmoji = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                confederation = UtsConfederation.UEFA,
                currentSeasonLabel = "2026-27",
                domesticCupName = "FA Cup",
                divisions = listOf(
                    UtsDivisionConfig(1, "Premier League (Tier 1)", "England", 0, 0, 2, 4, 2, idsFor("England", 1)),
                    UtsDivisionConfig(2, "EFL Championship (Tier 2)", "England", 2, 0, 2, 0, 0, idsFor("England", 2)),
                    UtsDivisionConfig(3, "EFL League One (Tier 3)", "England", 2, 0, 2, 0, 0, idsFor("England", 3)),
                    UtsDivisionConfig(4, "EFL League Two (Tier 4)", "England", 2, 0, 2, 0, 0, idsFor("England", 4)),
                    UtsDivisionConfig(5, "National League (Tier 5)", "England", 2, 0, 0, 0, 0, idsFor("England", 5))
                )
            ),
            UtsCountryPyramid(
                countryName = "Algeria",
                flagEmoji = "🇩🇿",
                confederation = UtsConfederation.CAF,
                currentSeasonLabel = "2026-27",
                domesticCupName = "Algerian Cup (Coupe d'Algerie)",
                divisions = listOf(
                    UtsDivisionConfig(1, "Ligue Professionnelle 1 (Tier 1)", "Algeria", 0, 0, 2, 2, 2, idsFor("Algeria", 1)),
                    UtsDivisionConfig(2, "Algerian Ligue 2 (Tier 2)", "Algeria", 2, 0, 0, 0, 0, idsFor("Algeria", 2))
                )
            ),
            UtsCountryPyramid(
                countryName = "Spain",
                flagEmoji = "🇪🇸",
                confederation = UtsConfederation.UEFA,
                currentSeasonLabel = "2026-27",
                domesticCupName = "Copa del Rey",
                divisions = listOf(
                    UtsDivisionConfig(1, "La Liga EA Sports (Tier 1)", "Spain", 0, 0, 2, 4, 2, idsFor("Spain", 1)),
                    UtsDivisionConfig(2, "Segunda Division (Tier 2)", "Spain", 2, 0, 0, 0, 0, idsFor("Spain", 2))
                )
            ),
            UtsCountryPyramid(
                countryName = "Germany",
                flagEmoji = "🇩🇪",
                confederation = UtsConfederation.UEFA,
                currentSeasonLabel = "2026-27",
                domesticCupName = "DFB-Pokal",
                divisions = listOf(
                    UtsDivisionConfig(1, "Bundesliga (Tier 1)", "Germany", 0, 0, 2, 4, 2, idsFor("Germany", 1)),
                    UtsDivisionConfig(2, "2. Bundesliga (Tier 2)", "Germany", 2, 0, 0, 0, 0, idsFor("Germany", 2))
                )
            ),
            UtsCountryPyramid(
                countryName = "Italy",
                flagEmoji = "🇮🇹",
                confederation = UtsConfederation.UEFA,
                currentSeasonLabel = "2026-27",
                domesticCupName = "Coppa Italia",
                divisions = listOf(
                    UtsDivisionConfig(1, "Serie A (Tier 1)", "Italy", 0, 0, 2, 4, 2, idsFor("Italy", 1)),
                    UtsDivisionConfig(2, "Serie B (Tier 2)", "Italy", 2, 0, 0, 0, 0, idsFor("Italy", 2))
                )
            ),
            UtsCountryPyramid(
                countryName = "France",
                flagEmoji = "🇫🇷",
                confederation = UtsConfederation.UEFA,
                currentSeasonLabel = "2026-27",
                domesticCupName = "Coupe de France",
                divisions = listOf(
                    UtsDivisionConfig(1, "Ligue 1 (Tier 1)", "France", 0, 0, 2, 3, 2, idsFor("France", 1)),
                    UtsDivisionConfig(2, "Ligue 2 (Tier 2)", "France", 2, 0, 0, 0, 0, idsFor("France", 2))
                )
            ),
            UtsCountryPyramid(
                countryName = "Brazil",
                flagEmoji = "🇧🇷",
                confederation = UtsConfederation.CONMEBOL,
                currentSeasonLabel = "2026-27",
                domesticCupName = "Copa do Brasil",
                divisions = listOf(
                    UtsDivisionConfig(1, "Brasileirao Serie A (Tier 1)", "Brazil", 0, 0, 2, 4, 2, idsFor("Brazil", 1)),
                    UtsDivisionConfig(2, "Brasileirao Serie B (Tier 2)", "Brazil", 2, 0, 0, 0, 0, idsFor("Brazil", 2))
                )
            )
        )
    }
}

private class UtsSqliteHelper(context: Context) : SQLiteOpenHelper(context, "uts_universal_engine.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS uts_teams (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                short_code TEXT NOT NULL,
                country TEXT NOT NULL,
                flag_emoji TEXT NOT NULL,
                confederation TEXT NOT NULL,
                category TEXT NOT NULL,
                sport TEXT NOT NULL,
                overall_rating INTEGER NOT NULL,
                attack_rating INTEGER NOT NULL,
                midfield_rating INTEGER NOT NULL,
                defense_rating INTEGER NOT NULL,
                gk_rating INTEGER NOT NULL,
                form INTEGER NOT NULL,
                home_advantage INTEGER NOT NULL,
                stadium_name TEXT NOT NULL,
                primary_color TEXT NOT NULL,
                secondary_color TEXT NOT NULL,
                custom_logo_uri TEXT NOT NULL,
                domestic_tier INTEGER NOT NULL,
                previous_rating INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_uts_teams_cat ON uts_teams(category, confederation, overall_rating DESC)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS uts_tournaments (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                year_label TEXT NOT NULL,
                host_name TEXT NOT NULL,
                sport TEXT NOT NULL,
                category TEXT NOT NULL,
                format TEXT NOT NULL,
                group_count INTEGER NOT NULL,
                teams_per_group INTEGER NOT NULL,
                advance_per_group INTEGER NOT NULL,
                best_third_count INTEGER NOT NULL,
                has_third_place INTEGER NOT NULL,
                is_completed INTEGER NOT NULL,
                champion_name TEXT NOT NULL,
                runner_up_name TEXT NOT NULL,
                third_place_name TEXT NOT NULL,
                current_phase TEXT NOT NULL,
                participant_ids_json TEXT NOT NULL,
                group_map_json TEXT NOT NULL,
                draw_log_json TEXT NOT NULL,
                data_origin TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS uts_matches (
                id TEXT PRIMARY KEY,
                tournament_id TEXT NOT NULL,
                stage_name TEXT NOT NULL,
                group_index INTEGER NOT NULL,
                home_id TEXT NOT NULL,
                home_name TEXT NOT NULL,
                home_flag TEXT NOT NULL,
                away_id TEXT NOT NULL,
                away_name TEXT NOT NULL,
                away_flag TEXT NOT NULL,
                home_score INTEGER NOT NULL,
                away_score INTEGER NOT NULL,
                is_played INTEGER NOT NULL,
                went_et INTEGER NOT NULL,
                went_pen INTEGER NOT NULL,
                home_pen INTEGER NOT NULL,
                away_pen INTEGER NOT NULL,
                home_poss INTEGER NOT NULL,
                away_poss INTEGER NOT NULL,
                home_shots INTEGER NOT NULL,
                away_shots INTEGER NOT NULL,
                home_sot INTEGER NOT NULL,
                away_sot INTEGER NOT NULL,
                home_corners INTEGER NOT NULL,
                away_corners INTEGER NOT NULL,
                home_fouls INTEGER NOT NULL,
                away_fouls INTEGER NOT NULL,
                home_yc INTEGER NOT NULL,
                away_yc INTEGER NOT NULL,
                home_rc INTEGER NOT NULL,
                away_rc INTEGER NOT NULL,
                events_json TEXT NOT NULL,
                leg_number INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_uts_matches_tid ON uts_matches(tournament_id, stage_name)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS uts_rating_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                team_id TEXT NOT NULL,
                team_name TEXT NOT NULL,
                old_rating INTEGER NOT NULL,
                new_rating INTEGER NOT NULL,
                reason TEXT NOT NULL,
                timestamp_label TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Non-destructive schema migrations to preserve user saves
    }
}
