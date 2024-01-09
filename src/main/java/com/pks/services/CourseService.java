package com.pks.services;

import com.pks.models.Answer;
import com.pks.models.Course;
import com.pks.models.Task;
import com.pks.models.User;
import com.pks.repositories.CourseRepository;
import com.pks.repositories.TaskRepository;
import com.pks.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public List<Course> listCourses(String title) {
        if (title != null) return courseRepository.findByTitle(title);
        return courseRepository.findAll();
    }

    public void addAnswer(User user, Task task, Answer answer){
        task.addAnswer(answer);
        userRepository.save(user);
        taskRepository.save(task);
    }

    public void userCourse(User user, Course course){
        user.addCourse(course);
        course.addUser(user);
        userRepository.save(user);
        courseRepository.save(course);
    }

    public User getUserByPrincipal(Principal principal) {
        if (principal == null) return new User();
        return userRepository.findByEmail(principal.getName());
    }


    public Course getCourseById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }
    public List<Course> listCourses() {
        return courseRepository.findAll();
    }

    public List<Task> findTasksByIdCourse(Long idCourse) {
        List<Task> newList = new ArrayList<>();
        for (Task task: taskRepository.findAll()) {
            if(task.getCourse().getId().equals(idCourse)){
                newList.add(task);
            }
        }
        return newList;
    }


    public List<Task> listTasks(Long idCourse) {
        List<Task> list = findTasksByIdCourse(idCourse);
        Comparator<Task> compareByNamber = new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return o1.getNamber()-o2.getNamber();
            }
        };
        Collections.sort(list, compareByNamber);
        return list;
    }

    public boolean userAnswerPrew(User user, Task taskNext) {
        Task task = foundPrewTask(taskNext);
        if (task.equals(taskNext)){
            return true;
        }
        for (Answer answer: task.getAnswers()){
            if(answer.getUser().equals(user)){
                return true;
            }
        }
        return  false;
    }

    public boolean prewAnswerTime(User user, Task taskNext) {
        Task task = foundPrewTask(taskNext);
        if (task.equals(taskNext)){
            return true;
        }
        for (Answer answer: task.getAnswers()){
            if(answer.getUser().equals(user)){
                if((answer.getDate().getDayOfYear() < LocalDateTime.now().getDayOfYear()) &
                        (answer.getDate().getYear()<LocalDateTime.now().getYear())){
                    return true;
                }
                return  false;
            }
        }
        return false;
    }

    private Task foundPrewTask(Task taskNext) {
        for (Task task: taskNext.getCourse().getTasks()){
            if (task.getNamber()==task.getNamber()+1){
                return task;
            }
        }
        return taskNext;
    }
}
