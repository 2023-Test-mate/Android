package kr.hs.emirim.evie.testmateloginpage.goalList.data

data class GoalResponse(
    val goalId: Int,
    val subjectId: Int,
    val semester: Int,
    var goal: String,
    var completed: Boolean
)

fun GoalResponse.toGoalPatchRequest(): GoalPatchRequest {
    return GoalPatchRequest(
        goal = this.goal,
        completed = this.completed
    )
}