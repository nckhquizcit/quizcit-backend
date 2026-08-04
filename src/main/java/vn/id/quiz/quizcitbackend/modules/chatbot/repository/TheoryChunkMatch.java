package vn.id.quiz.quizcitbackend.modules.chatbot.repository;

public record TheoryChunkMatch(
        String chunkId,
        String theoryId,
        String chapterId,
        String content,
        double distance) {
}
