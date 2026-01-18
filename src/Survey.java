import model.Form;
import model.Submission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Survey {
    HashMap<Integer, Form> surveys = new HashMap<>();
    ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Submission>> submissions = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, Integer> survey_rating = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Integer> user_ratings = new ConcurrentHashMap<>();
    // Maintaining count using atomic integer is necessary
    ConcurrentHashMap<Integer, AtomicInteger> sub_count = new ConcurrentHashMap<>();

    public void createSurvey(int survey_id, HashMap<Integer, String> questions, HashMap<Integer, Integer> weights) throws Exception{
        if(surveys.containsKey(survey_id)){
            throw new Exception("survey already present");
        }
        if(questions.size()!=weights.size()){
            throw new Exception("invalid survey format");
        }
        surveys.put(survey_id, new Form(survey_id, questions, weights));
        submissions.put(survey_id, new ConcurrentLinkedQueue<>());
        survey_rating.put(survey_id, 0);
        sub_count.put(survey_id, new AtomicInteger(0));
    }

    public int postSurvey(int survey_id, String user_id, HashMap<Integer, Integer> weights) throws Exception{
        Submission submission = new Submission(survey_id, user_id, weights);
        if(!surveys.containsKey(survey_id)){
            throw new Exception("no survey present");
        }

        ConcurrentLinkedQueue<Submission> submission_existing = submissions.get(survey_id);
        submission_existing.add(submission);
        int user_rating = computeRating(survey_id, submission);
        user_ratings.put(user_id, user_rating);
        survey_rating.compute(survey_id, (k, v) -> {
                return (sub_count.get(survey_id).get()*v+user_rating)/(sub_count.get(survey_id).get()+1);
        });
        sub_count.get(survey_id).incrementAndGet();
        System.out.println(STR."Survey rating for \{Thread.currentThread().getName()} is \{survey_rating.get(survey_id)}");
        return user_rating;
    }

    private int computeRating(int survey_id, Submission submission) throws Exception{
        Form form = surveys.get(survey_id);
        HashMap<Integer, Integer> weights = form.weights;
        int rating = 0;
        HashMap<Integer, Integer> s_weights = submission.weights;
        if(weights.size() != s_weights.size()){
            throw new Exception("Form field count not same");
        }
        for(Map.Entry<Integer, Integer> entry: s_weights.entrySet()){
            rating +=entry.getValue() * weights.get(entry.getKey());
        }
        return rating;
    }

    public int getSurveyRating(int survey_id) throws Exception{
        if(!survey_rating.containsKey(survey_id))
            throw new Exception("no survey exist");
        return survey_rating.get(survey_id);
    }

    public int getUserRating(String user_id) throws Exception{
        if(!user_ratings.containsKey(user_id))
            throw new Exception("no survey exist");
        return user_ratings.get(user_id);
    }
}
