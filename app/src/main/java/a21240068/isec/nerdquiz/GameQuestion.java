package a21240068.isec.nerdquiz;

import java.util.ArrayList;

/**
 * Created by bernardovieira on 07-11-2016.
 */

public class GameQuestion
{
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
