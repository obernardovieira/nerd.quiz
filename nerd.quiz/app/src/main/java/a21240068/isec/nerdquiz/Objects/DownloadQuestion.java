package a21240068.isec.nerdquiz.Objects;

import java.io.Serializable;

/**
 * Created by bernardovieira on 08-12-2016.
 */

public class DownloadQuestion extends Object implements Serializable {

    static final long serialVersionUID = 42L;

    private String type;
    private String question;
    private String ranswer;

    public DownloadQuestion(String type, String question, String ranswer) {
        this.type = type;
        this.question = question;
        this.ranswer = ranswer;
    }

    public String getType() {
        return type;
    }

    public String getQuestion() {
        return question;
    }

    public String getRanswer() {
        return ranswer;
    }

}

