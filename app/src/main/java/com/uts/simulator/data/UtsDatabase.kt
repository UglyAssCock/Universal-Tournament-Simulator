package com.uts.simulator.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY overallRating DESC, name ASC")
    fun observeAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams ORDER BY overallRating DESC, name ASC")
    suspend fun getAllTeamsOnce(): List<TeamEntity>

    @Query("SELECT * FROM teams WHERE category = :category ORDER BY overallRating DESC, name ASC")
    suspend fun getTeamsByCategory(category: TeamCategory): List<TeamEntity>

    @Query("SELECT * FROM teams WHERE category = :category AND confederation = :confederation ORDER BY overallRating DESC, name ASC")
    suspend fun getTeamsByCategoryAndConfed(category: TeamCategory, confederation: Confederation): List<TeamEntity>

    @Query("SELECT * FROM teams WHERE category = 'CLUB_MEN' AND country = :country ORDER BY domesticDivisionTier ASC, overallRating DESC")
    suspend fun getClubsByCountry(country: String): List<TeamEntity>

    @Query("SELECT * FROM teams WHERE id IN (:ids)")
    suspend fun getTeamsByIds(ids: List<Long>): List<TeamEntity>

    @Query("SELECT * FROM teams WHERE id = :id LIMIT 1")
    suspend fun getTeamById(id: Long): TeamEntity?

    @Query("SELECT COUNT(*) FROM teams")
    suspend fun getTeamCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity): Long

    @Update
    suspend fun updateTeam(team: TeamEntity)

    @Delete
    suspend fun deleteTeam(team: TeamEntity)
}

@Dao
interface HistoricalDao {
    @Query("SELECT * FROM historical_records ORDER BY yearLabel DESC")
    fun observeAllHistorical(): Flow<List<HistoricalEditionEntity>>

    @Query("SELECT * FROM historical_records WHERE competitionCategory = :category ORDER BY yearLabel DESC")
    suspend fun getHistoricalByCategory(category: String): List<HistoricalEditionEntity>

    @Query("SELECT COUNT(*) FROM historical_records")
    suspend fun getHistoricalCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoricalEditions(editions: List<HistoricalEditionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoricalEdition(edition: HistoricalEditionEntity): Long
}

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY createdAt DESC")
    fun observeAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE seasonSaveId = :seasonSaveId ORDER BY divisionTier ASC, id ASC")
    suspend fun getTournamentsForSeasonSave(seasonSaveId: Long): List<TournamentEntity>

    @Query("SELECT * FROM tournaments WHERE id = :id LIMIT 1")
    suspend fun getTournamentById(id: Long): TournamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity): Long

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Query("DELETE FROM tournaments WHERE id = :id")
    suspend fun deleteTournamentById(id: Long)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY roundNumber ASC, matchOrder ASC")
    fun observeMatchesForTournament(tournamentId: Long): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY roundNumber ASC, matchOrder ASC")
    suspend fun getMatchesForTournamentOnce(tournamentId: Long): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE isPlayed = 1 ORDER BY id DESC LIMIT 100")
    fun observeRecentPlayedMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE isPlayed = 1")
    suspend fun getAllPlayedMatchesOnce(): List<MatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE tournamentId = :tournamentId")
    suspend fun deleteMatchesForTournament(tournamentId: Long)
}

@Dao
interface SeasonSaveDao {
    @Query("SELECT * FROM season_saves ORDER BY lastUpdated DESC")
    fun observeSeasonSaves(): Flow<List<SeasonSaveEntity>>

    @Query("SELECT * FROM season_saves WHERE id = :id LIMIT 1")
    suspend fun getSeasonSaveById(id: Long): SeasonSaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeasonSave(save: SeasonSaveEntity): Long

    @Update
    suspend fun updateSeasonSave(save: SeasonSaveEntity)

    @Query("DELETE FROM season_saves WHERE id = :id")
    suspend fun deleteSeasonSave(id: Long)
}

@Database(
    entities = [
        TeamEntity::class,
        HistoricalEditionEntity::class,
        TournamentEntity::class,
        MatchEntity::class,
        SeasonSaveEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(UtsTypeConverters::class)
abstract class UtsDatabase : RoomDatabase() {
    abstract fun teamDao(): TeamDao
    abstract fun historicalDao(): HistoricalDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun matchDao(): MatchDao
    abstract fun seasonSaveDao(): SeasonSaveDao

    companion object {
        @Volatile
        private var INSTANCE: UtsDatabase? = null

        fun getInstance(context: Context): UtsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UtsDatabase::class.java,
                    "universal_tournament_simulator.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
