package a21240068.isec.nerdquiz.Objects;


import java.io.Serializable;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bernardovieira
 */
public class GameQuestion implements Serializable
{
    static final long serialVersionUID = 42L;
    
    private String question;
    private String r_answer;
    private ArrayList<String> answers;

    public GameQuestion()
    {
        answers = new ArrayList<>();
    }

    public String getQuestion()
    {
        return question;
    }

    public String getRightAnswer()
    {
        return r_answer;
    }

    public ArrayList<String> getAnswers()
    {
        return answers;
    }

    public void addAnswers(ArrayList<String> answers)
    {
        this.answers = answers;
    }

    public void setQuestion(String question)
    {
        this.question = question;
    }

    public void setRightAnswer(String r_answer)
    {
        this.r_answer = r_answer;
    }
}
