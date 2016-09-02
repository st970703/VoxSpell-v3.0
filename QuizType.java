public enum QuizType {
	NEW("wordList"),
	REVIEW("failedList")
	;

	private final String quizType;

	private QuizType(String quiz) {
		this.quizType = quiz;
	}

	public String getQuizType() {
		return quizType;
	}
}