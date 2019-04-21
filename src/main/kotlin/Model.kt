import java.time.Duration
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

const val pattern = "yyyy-MM-dd"
val formatter = DateTimeFormatter.ofPattern(pattern)!!
const val CIGARETTE_PRICE: Long = 5000 / 20

enum class Sex {
    MALE,
    FEMALE
}

enum class Plan {
    START_ABSTENTION,
    KEEP_SMOKING,
    ALREADY_STARTED_ABSTENTION,
}

data class InputProfile(
    val sex: Sex,
    val birthDay: LocalDateTime,
    val plan: Plan,
    val cigarettesPerDay: Int,
    val smokingDuration: Duration,
    val nonSmokingDuration: Duration
)

data class TimeSegment(
    val smoking: Boolean,
    val startTime: LocalDateTime,
    var endTime: LocalDateTime? = null,
    var consumsion: Long = 0
)

data class UserRecord(
    val sex: Sex,
    val birthDay: LocalDateTime,
    val cigarettesPerDay: Int,
    val segments: List<TimeSegment>
)

interface Clock {
    val now: LocalDateTime
}

class UserData {
    private val clock: Clock
    private val segments: MutableList<TimeSegment> = mutableListOf()
    val sex: Sex
    val birthDay: LocalDateTime
    val cigarettesPerDay: Int
    val smoking: Boolean get() = lastSegment.smoking
    val lastSegment: TimeSegment get() = segments.last()
    val smokingDuration: Duration
        get() {
            return when (lastSegment.smoking) {
                true -> Duration.between(lastSegment.startTime, clock.now)
                false -> Duration.ZERO
            }
        }
    val nonSmokingDuration: Duration
        get() {
            return when (lastSegment.smoking) {
                true -> Duration.ZERO
                false -> Duration.between(lastSegment.startTime, clock.now)
            }
        }
    val saving: Long get() {
        val amount: Double = when(lastSegment.smoking) {
            true -> -CIGARETTE_PRICE * cigarettesPerDay * (smokingDuration.toMillis() / 1000.0 / 60 / 60 / 24)
            false -> CIGARETTE_PRICE * cigarettesPerDay * (nonSmokingDuration.toMillis() / 1000.0 / 60 / 60 / 24)
        }

        return amount.toLong() - lastSegment.consumsion
    }
    val age: Int get() = Period.between(birthDay.toLocalDate(), clock.now.toLocalDate()).years

    constructor(clock: Clock, inputProfile: InputProfile) {
        this.clock = clock
        sex = inputProfile.sex
        birthDay = inputProfile.birthDay
        cigarettesPerDay = inputProfile.cigarettesPerDay

        val segment: TimeSegment = when (inputProfile.plan) {
            Plan.START_ABSTENTION -> {
                TimeSegment(false, clock.now)
            }
            Plan.KEEP_SMOKING -> {
                val now = clock.now
                val startTime = now - inputProfile.smokingDuration
                TimeSegment(true, startTime)
            }
            Plan.ALREADY_STARTED_ABSTENTION -> {
                val now = clock.now
                val startTime = now - inputProfile.nonSmokingDuration
                TimeSegment(false, startTime)
            }
        }

        segments.add(segment)
    }

    constructor(clock: Clock, userRecord: UserRecord) {
        this.clock = clock
        sex = userRecord.sex
        birthDay = userRecord.birthDay
        cigarettesPerDay = userRecord.cigarettesPerDay
        segments.addAll(userRecord.segments)

        if (lastSegment.endTime != null) {
            addNewSegment(!lastSegment.smoking)
        }
    }

    fun startAbstention() {
        addNewSegment(false)
    }

    fun stopAbstention() {
        addNewSegment(true)
    }

    private fun addNewSegment(smoking: Boolean) {
        val now = clock.now
        lastSegment.endTime = now
        segments.add(TimeSegment(smoking, now))
    }

    fun consume(amount: Long): Boolean {
        if (!lastSegment.smoking) {
            lastSegment.consumsion += amount
            return true
        }

        return false
    }
}