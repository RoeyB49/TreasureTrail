import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.treasuretrail.models.User


@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUser(id: String): LiveData<User>

    @Update
    suspend fun updateUser(user: User)
}
