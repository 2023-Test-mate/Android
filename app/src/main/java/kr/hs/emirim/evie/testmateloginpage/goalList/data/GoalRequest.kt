package kr.hs.emirim.evie.testmateloginpage.goalList.data

data class GoalRequest(
    val subjectId: Int,
    val semester: Int,
    var goal: String,
    val completed: Boolean
)
