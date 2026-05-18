package ru.gr0946x.db.repository;

import ru.gr0946x.db.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT m FROM Message m WHERE (m.senderId = :user1Id AND m.receiverId = :user2Id) OR (m.senderId = :user2Id AND m.receiverId = :user1Id) ORDER BY m.timestamp DESC")
    List<Message> findLastMessages(@Param("user1Id") int user1Id,
                                   @Param("user2Id") int user2Id,
                                   Pageable pageable);

    @Query("SELECT m FROM Message m WHERE ((m.senderId = :user1Id AND m.receiverId = :user2Id) OR (m.senderId = :user2Id AND m.receiverId = :user1Id)) AND LOWER(m.text) LIKE LOWER(CONCAT('%', :searchText, '%')) ORDER BY m.timestamp DESC")
    List<Message> searchMessagesJPQL(@Param("user1Id") int user1Id,
                                     @Param("user2Id") int user2Id,
                                     @Param("searchText") String searchText);

}