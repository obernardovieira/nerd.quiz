package a21240068.isec.nerdquiz.Objects;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;

/**
 *
 * @author bernardovieira
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