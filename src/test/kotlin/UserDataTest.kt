import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class UserDataTest {
    class TestClock : Clock {
        private var _now: LocalDateTime = LocalDateTime.of(2019, 1, 1, 0, 0)

        override val now: LocalDateTime
            get() = _now

        fun goForward(duration: Duration) {
            _now += duration
        }
    }

    @Test
    fun `test user data`() {
        val inputProfile = InputProfile(
            Sex.MALE,
            LocalDateTime.of(1979, 6, 29, 0, 0),
            Plan.ALREADY_STARTED_ABSTENTION,
            5,
            Duration.ofDays(5),
            Duration.ofDays(1)
        )

        val clock = TestClock()
        val userData = UserData(clock, inputProfile)
        assert(!userData.smoking)
        assert(userData.smokingDuration == Duration.ZERO)

        clock.goForward(Duration.ofDays(3))

        assert(userData.nonSmokingDuration == Duration.ofDays(4))
        assert(userData.saving == CIGARETTE_PRICE * 4  * userData.cigarettesPerDay)


        userData.stopAbstention()
        val twoDays = Duration.ofDays(2)
        clock.goForward(twoDays)
        assert(userData.smoking)
        assert(userData.saving == -CIGARETTE_PRICE * twoDays.toDays()  * userData.cigarettesPerDay)
    }
}