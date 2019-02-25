package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class QuestionsState implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("questions")
    private List<Question> questions;
    private String status;

    public List<Question> getQuestions() {
        return questions;
    }

    public String getStatus() {
        return status;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}