import java.util.*;

public class CourseGradebook extends Gradebook {

    private Map<String, Map<Student, Double>> assignmentScores = new HashMap<>();
    private Map<String, Stack<ScoreChange>> undoStacks = new HashMap<>();
    private Map<String, Queue<Student>> lateSubmissions = new HashMap<>();

    @Override
    public void setScore(String assignmentName, Student student, double score) {
        assignmentScores.putIfAbsent(assignmentName, new HashMap<>());
        Map<Student, Double> scores = assignmentScores.get(assignmentName);

        double previousScore = scores.getOrDefault(student, Double.NaN);
        scores.put(student, score);

        // Track the change for undo
        undoStacks.putIfAbsent(assignmentName, new Stack<>());
        undoStacks.get(assignmentName).push(new ScoreChange(student, previousScore));
    }

    @Override
    public double getScore(String assignmentName, Student student) {
        if (assignmentScores.containsKey(assignmentName)) {
            return assignmentScores.get(assignmentName)
                                   .getOrDefault(student, Double.NaN);
        }
        return Double.NaN;
    }

    @Override
    public Map<Integer, Double> getAssignmentScores(String assignmentName) {
        Map<Integer, Double> result = new HashMap<>();
        Map<Student, Double> scores = assignmentScores.get(assignmentName);
        if (scores != null) {
            for (Map.Entry<Student, Double> entry : scores.entrySet()) {
                result.put(entry.getKey().getId(), entry.getValue());
            }
        }
        return result;
    }

    @Override
    public Map<String, Double> getStudentScores(Student student) {
        Map<String, Double> result = new HashMap<>();
        for (String assignment : assignmentScores.keySet()) {
            Map<Student, Double> scores = assignmentScores.get(assignment);
            if (scores.containsKey(student)) {
                result.put(assignment, scores.get(student));
            }
        }
        return result;
    }

    @Override
    public ArrayList<String> getSortedAssignmentNames() {
        ArrayList<String> names = new ArrayList<>(assignmentScores.keySet());
        Collections.sort(names);
        return names;
    }

    @Override
    public ArrayList<Student> getSortedStudents() {
        Set<Student> allStudents = new HashSet<>();
        for (Map<Student, Double> map : assignmentScores.values()) {
            allStudents.addAll(map.keySet());
        }
        ArrayList<Student> sorted = new ArrayList<>(allStudents);
        Collections.sort(sorted);
        return sorted;
    }

    // BONUS: Undo score
    @Override
    public boolean undoLastScoreChange(String assignmentName) {
        Stack<ScoreChange> stack = undoStacks.get(assignmentName);
        if (stack != null && !stack.isEmpty()) {
            ScoreChange change = stack.pop();
            if (Double.isNaN(change.previousScore)) {
                assignmentScores.get(assignmentName).remove(change.student);
            } else {
                assignmentScores.get(assignmentName).put(change.student, change.previousScore);
            }
            return true;
        }
        return false;
    }

    // BONUS: Late submission queue
    @Override
    public void addLateSubmission(String assignmentName, Student student) {
        lateSubmissions.putIfAbsent(assignmentName, new LinkedList<>());
        lateSubmissions.get(assignmentName).add(student);
    }

    @Override
    public Student processNextLateSubmission(String assignmentName) {
        Queue<Student> queue = lateSubmissions.get(assignmentName);
        if (queue != null && !queue.isEmpty()) {
            return queue.poll();
        }
        return null;
    }

    // Helper inner class for undo tracking
    private static class ScoreChange {
        Student student;
        double previousScore;

        ScoreChange(Student student, double previousScore) {
            this.student = student;
            this.previousScore = previousScore;
        }
    }
}
