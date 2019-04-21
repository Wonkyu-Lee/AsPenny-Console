import java.lang.StringBuilder
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.util.*
import java.time.format.DateTimeParseException

fun main(args: Array<String>) {
    fun askSex(scanner: Scanner): Sex {
        var sex: Sex? = null
        while (sex == null) {
            println("성별(m: 남, f: 여)?")
            val input = scanner.nextLine()
            sex = when (input) {
                "m" -> Sex.MALE
                "f" -> Sex.FEMALE
                else -> null
            }
        }
        return sex
    }

    fun askBirthDay(scanner: Scanner): LocalDateTime {
        var birthDay: LocalDate? = null
        while (birthDay == null) {
            println("생일($pattern)?")
            val input = scanner.nextLine()
            try {
                birthDay = LocalDate.parse(input, formatter)
            } catch (e: DateTimeParseException) {
                // do nothing
            }
        }
        return birthDay.atTime(0, 0)
    }

    fun askPlan(scanner: Scanner): Plan {
        var plan: Plan? = null
        while (plan == null) {
            println("금연할 건가요?")
            println("\t1. 네, 이번엔 제발")
            println("\t2. 아니요, 아직 생각 없어요")
            println("\t3. 금연 중이에요")
            val input = scanner.nextInt()
            plan = when (input) {
                1 -> Plan.START_ABSTENTION
                2 -> Plan.KEEP_SMOKING
                3 -> Plan.ALREADY_STARTED_ABSTENTION
                else -> null
            }
        }
        return plan
    }

    fun askDuration(smoking: Boolean, age: Int, scanner: Scanner): Duration {
        var duration: Duration? = null
        while (duration == null) {
            val type = when (smoking) {
                true -> "흡연기간"
                false -> "금연기간"
            }
            println("${type}이 얼마나 되나요(년 월)?")
            var years = scanner.nextInt()
            if (years < 0 || age <= years)
                continue

            var months = scanner.nextInt()
            years += months / 12
            months %= 12

            val days = ((years + months/12.0f) * 365).toLong()
            duration = Duration.ofDays(days)
        }

        return duration
    }

    fun askCigarettesPerDay(scanner: Scanner): Int {
        println("하루에 몇 개비 피우나요?")
        return scanner.nextInt()
    }

    fun getDurationText(minus: Boolean, duration: Duration): String {
        var remains = duration
        val days = remains.toDays()
        remains = remains.minus(Duration.ofDays(days))
        val hours = remains.toHours()
        remains = remains.minus(Duration.ofHours(hours))
        val minutes = remains.toMinutes()
        remains = remains.minus(Duration.ofMinutes(minutes))
        val seconds = remains.seconds

        return if (minus) "-$days Days" else "$days Days, ${hours}h ${minutes}m ${seconds}s"
    }

    fun getView(userData: UserData): String {
        return when (userData.smoking) {
            true -> {
                with (StringBuilder()) {
                    appendln("금연을 시작하세요")
                    appendln(getDurationText(true, userData.smokingDuration))
                    appendln("${userData.saving} 원")

                }.toString()
            }
            false -> {
                with (StringBuilder()) {
                    appendln("start: ${userData.lastSegment.startTime.format(formatter)}")
                    appendln(getDurationText(false, userData.nonSmokingDuration))
                    appendln("${userData.saving} 원")
                }.toString()
            }
        }
    }

    fun getProfile(userData: UserData): String {
        val sb = StringBuilder()
        return with (userData) {
            sb.appendln("+++++++++++++++++++++++++")
            sb.appendln("성별: ${if (sex == Sex.MALE) "남자" else "여자"}")
            sb.appendln("생일: ${birthDay.format(formatter)}")
            sb.appendln("나이: $age")
            sb.appendln("하루 피우는 양: $cigarettesPerDay 개비")
        }.toString()
    }

    fun showMenu(userData: UserData, scanner: Scanner) {
        var quit = false
        while (!quit) {
            scanner.reset()
            println("=======================================")
            println(getView(userData))
            println(".......................................")
            println("무얼 하고 싶으신가요?")

            println("\tr. 갱신 조회")

            println("\tp. 프로필 조회")

            if (userData.smoking) {
                println("\ts. 금연 시작")
            } else {
                println("\te. 피워버렸어요")
            }

            if (userData.saving > 0) {
                println("\tc. 500원 소비")
            }

            println("\tq. 종료")

            val input = scanner.nextLine()
            when (input) {
                "p" -> println(getProfile(userData))
                "s" -> userData.startAbstention()
                "e" -> userData.stopAbstention()
                "c" -> userData.consume(500)
                "q" -> quit = true
            }
        }
    }

    val clock: Clock = object: Clock {
        override val now: LocalDateTime
            get() = LocalDateTime.now()
    }

    fun loadFromProfile(scanner: Scanner): UserData {
        val sex = askSex(scanner)
        val birthDay = askBirthDay(scanner)
        val age = Period.between(birthDay.toLocalDate(), LocalDate.now()).years
        val smokingDuration = askDuration(true, age, scanner)
        val cigarettesPerDay = askCigarettesPerDay(scanner)
        val plan = askPlan(scanner)

        val profile = when (plan) {
            Plan.START_ABSTENTION, Plan.KEEP_SMOKING -> InputProfile(sex, birthDay, plan, cigarettesPerDay, smokingDuration, Duration.ZERO)
            Plan.ALREADY_STARTED_ABSTENTION -> {
                val nonSmokingDuration = askDuration(false, age, scanner)
                InputProfile(sex, birthDay, plan, cigarettesPerDay, smokingDuration, nonSmokingDuration)
            }
        }

        return UserData(clock, profile)
    }

    val userRecord = UserRecord(
        Sex.MALE,
        LocalDateTime.of(1979, 6, 29, 0, 0),
        80,
        listOf(TimeSegment(true, LocalDateTime.now() - Duration.ofDays(100)))
    )

    fun loadFromRecord(userRecord: UserRecord): UserData {
        return UserData(clock, userRecord)
    }

    fun askUserData(scanner: Scanner): UserData {
        var userData: UserData? = null

        while (userData == null) {
            println("어떻게 시작할까?")
            println("\t1. 새 프로필을 작성한다")
            println("\t2. 기존 데이터를 로딩한다")
            val selection = scanner.nextInt()
            scanner.nextLine() // flush after nextInt()
            userData = when (selection) {
                1 -> loadFromProfile(scanner)
                2 -> loadFromRecord(userRecord)
                else -> null
            }
        }

        return userData
    }

    with(Scanner(System.`in`)) {
        val userData = askUserData(this)
        showMenu(userData, this)
    }
}