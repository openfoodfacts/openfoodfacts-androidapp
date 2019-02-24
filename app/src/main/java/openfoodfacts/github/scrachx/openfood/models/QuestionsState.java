package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionsState implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("questions")
    private List<Questions> questions;
    private String status;

    public List<Questions> getQuestions() {
        return questions;
    }

    public String getStatus() {
        return status;
    }

    public void setQuestions(List<Questions> questions) {
        this.questions = questions;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
