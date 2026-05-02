package vn.id.quiz.quizcitbackend.modules.content.domain.repository;

import vn.id.quiz.quizcitbackend.modules.content.domain.entity.Theory;

import java.util.List;
import java.util.Optional;

public interface TheoryRepository {
    Optional<Theory> findById(String id);
    List<Theory> findAll();
    Theory save(Theory theory);
    void deleteById(String id);
    boolean existsById(String id);
}
