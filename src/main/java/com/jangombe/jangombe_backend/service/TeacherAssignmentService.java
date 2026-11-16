package com.jangombe.jangombe_backend.service;

import com.jangombe.jangombe_backend.entity.TeacherAssignment;
import com.jangombe.jangombe_backend.repository.ClassRepository;
import com.jangombe.jangombe_backend.repository.SubjectRepository;
import com.jangombe.jangombe_backend.repository.TeacherRepository;
import com.jangombe.jangombe_backend.repository.TeacherAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherAssignmentService {

    private final TeacherAssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;

    public TeacherAssignmentService(
            TeacherAssignmentRepository assignmentRepository,
            TeacherRepository teacherRepository,
            ClassRepository classRepository,
            SubjectRepository subjectRepository) {
        this.assignmentRepository = assignmentRepository;
        this.teacherRepository = teacherRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public TeacherAssignment createAssignment(TeacherAssignment assignment) {
        if (assignment == null
                || assignment.getTeacher() == null || assignment.getTeacher().getId() == null
                || assignment.getClassEntity() == null || assignment.getClassEntity().getId() == null
                || assignment.getSubject() == null || assignment.getSubject().getId() == null) {
            throw new IllegalArgumentException("Teacher, class, and subject are required");
        }

        Long teacherId = assignment.getTeacher().getId();
        Long classId = assignment.getClassEntity().getId();
        Long subjectId = assignment.getSubject().getId();
        assignment.setTeacher(teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Selected teacher was not found")));
        assignment.setClassEntity(classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Selected class was not found")));
        assignment.setSubject(subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Selected subject was not found")));

        if (assignmentRepository.existsByTeacherIdAndClassEntityIdAndSubjectId(teacherId, classId, subjectId)) {
            throw new IllegalArgumentException("This teacher is already assigned to that class and subject");
        }

        if (assignment.isClassTeacher()) {
            List<TeacherAssignment> classAssignments = assignmentRepository
                    .findByClassEntityId(classId);
            classAssignments.forEach(existing -> existing.setClassTeacher(false));
            assignmentRepository.saveAll(classAssignments);
        }
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public void setClassTeacherRole(Long teacherId, Long classId, boolean classTeacher) {
        List<TeacherAssignment> teacherAssignments = assignmentRepository
                .findByTeacherIdAndClassEntityId(teacherId, classId);
        if (teacherAssignments.isEmpty()) {
            throw new IllegalArgumentException("Assign the teacher to a subject in this class first");
        }

        if (classTeacher) {
            List<TeacherAssignment> classAssignments = assignmentRepository.findByClassEntityId(classId);
            classAssignments.forEach(assignment -> assignment.setClassTeacher(false));
            teacherAssignments.get(0).setClassTeacher(true);
            assignmentRepository.saveAll(classAssignments);
        } else {
            teacherAssignments.forEach(assignment -> assignment.setClassTeacher(false));
            assignmentRepository.saveAll(teacherAssignments);
        }
    }

    public List<TeacherAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public Optional<TeacherAssignment> getAssignmentById(Long id) {
        return assignmentRepository.findById(id);
    }

    public List<TeacherAssignment> getAssignmentsByTeacher(Long teacherId) {
        return assignmentRepository.findByTeacherId(teacherId);
    }

    public List<TeacherAssignment> getAssignmentsByClass(Long classId) {
        return assignmentRepository.findByClassEntityId(classId);
    }

    public List<TeacherAssignment> getAssignmentsBySubject(Long subjectId) {
        return assignmentRepository.findBySubjectId(subjectId);
    }

    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }
}