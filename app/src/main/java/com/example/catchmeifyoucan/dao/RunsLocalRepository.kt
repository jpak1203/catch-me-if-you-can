package com.example.catchmeifyoucan.dao

import com.example.catchmeifyoucan.ui.runs.RunsModel
import com.example.catchmeifyoucan.utils.EspressoIdlingResource
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemindersLocalRepository(
    private val runsDao: RunsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RunsDataSource {

    /**
     * Get the runs list from the local db
     * @return Result the holds a Success with all the reminders or an Error object with the error message
     */
    override suspend fun getAllRuns(uid: String): Result<List<RunsModel>> =
        EspressoIdlingResource.wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                return@withContext try {
                    Result.Success(runsDao.getAll(uid))
                } catch (ex: Exception) {
                    Result.Error(ex.localizedMessage)
                }
            }
        }

    /**
     * Insert a run in the db.
     * @param run the run to be inserted
     */
    override suspend fun saveRun(run: RunsModel) =
        EspressoIdlingResource.wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                runsDao.saveRun(run)
            }
        }

    /**
     * Get a run by its id
     * @param id to be used to get the run
     * @return Result the holds a Success object with the Reminder or an Error object with the error message
     */
    override suspend fun getRun(id: String): Result<RunsModel> =
        EspressoIdlingResource.wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                try {
                    val run = runsDao.loadRunById(id)
                    return@withContext Result.Success(run)
                } catch (e: Exception) {
                    return@withContext Result.Error(e.localizedMessage)
                }
            }
        }

    /**
     * Deletes a run in the db
     */
    override suspend fun deleteRun(run: RunsModel) {
        EspressoIdlingResource.wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                runsDao.delete(run)
            }
        }
    }
}