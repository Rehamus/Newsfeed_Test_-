package com.sparta.newsfeed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.newsfeed.dto.BoardDto.BoardRequestDto;
import com.sparta.newsfeed.dto.BoardDto.BoardResponseDto;
import com.sparta.newsfeed.entity.Board;
import com.sparta.newsfeed.entity.Likes.ContentsLike;
import com.sparta.newsfeed.entity.Likes.LikeContents;
import com.sparta.newsfeed.entity.Multimedia;
import com.sparta.newsfeed.entity.Users.User;
import com.sparta.newsfeed.jwt.util.JwtTokenProvider;
import com.sparta.newsfeed.repository.BoardRepository;
import com.sparta.newsfeed.repository.ContentsLikeRepository;
import com.sparta.newsfeed.repository.MultimediaRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final MultimediaRepository multimediaRepository;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwt;
    private final ContentsLikeRepository contentsLikeRepository;

    // 개시판 생성
    // HttpServletRequest 는 유저 정보 받아오는거
    @Transactional
    public String createBoard(HttpServletRequest servletRequest, BoardRequestDto boardRequestDto) {

        User user = jwt.getTokenUser(servletRequest);
        Board board = new Board(user, boardRequestDto);
        board.setLikecounts(0L);
        boardRepository.save(board);
        return board.getContents() + " 생성 완료";
    }

    // 개시판 만들때 파일도 같이 넣음
    @Transactional
    public String createMBoard(HttpServletRequest servletRequest, MultipartFile image, MultipartFile movie, String board) {
        try {
            User user = jwt.getTokenUser(servletRequest);
            Board new_board = new Board(user, getStringBoard(board));

            Multimedia multimedia = new Multimedia();
            multimedia.setBoard(new_board);

            if (image != null && !image.isEmpty() && image.getContentType() != null && image.getContentType().toLowerCase().contains("image")) {
                String imageKey = "images/" + UUID.randomUUID();
                uploadFileToS3(imageKey, image.getBytes(), image.getContentType());
                multimedia.setImageUrl(getS3Url(imageKey));
            }

            if (movie != null && !movie.isEmpty() && movie.getContentType() != null && (movie.getContentType().toLowerCase().contains("mp4") || movie.getContentType().toLowerCase().contains("avi"))) {
                String movieKey = "movies/" + UUID.randomUUID();
                uploadFileToS3(movieKey, movie.getBytes(), movie.getContentType());
                multimedia.setMovieUrl(getS3Url(movieKey));
            }
            new_board.setLikecounts(0L);
            boardRepository.save(new_board);
            multimediaRepository.save(multimedia);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "생성 완료";
    }


    // s3 사용
    public void uploadFileToS3(String key, byte[] bytes, String contentType) {
        if (bytes.length > 10 * 1024 * 1024 && contentType.toLowerCase().contains("image")) {
            throw new IllegalArgumentException("이미지 용량이 너무 큽니다. 최대 10MB까지 업로드 가능합니다.");
        } else if (bytes.length > 200 * 1024 * 1024 && (contentType.toLowerCase().contains("mp4") || contentType.toLowerCase().contains("avi"))) {
            throw new IllegalArgumentException("동영상 용량이 너무 큽니다. 최대 200MB까지 업로드 가능합니다.");
        }

        try (S3Client s3Client = S3Client.builder().region(Region.AP_NORTHEAST_2).build()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket("onebytenewsfeed")
                    .key(key)
                    .contentType(contentType)
                    .build();
            RequestBody requestBody = RequestBody.fromBytes(bytes);
            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, requestBody);
        }
    }

    public String getS3Url(String key) {
        return "https://onebytenewsfeed.s3.amazonaws.com/" + key;
    }

    // 개시판 전채 조회
    @Transactional
    public Page<BoardResponseDto> getAllBoard(HttpServletRequest servletRequest, int page, int view, LocalDateTime start, LocalDateTime end) {
        Sort sort = null;

        switch (view) {
            case 1 -> sort = Sort.by(Sort.Direction.DESC, "likecounts");
            case 2 -> sort = Sort.by(Sort.Direction.ASC, "likecounts");
            case 3 -> {
                if (start == null || end == null) throw new IllegalArgumentException("start와 end 날짜는 필수입니다.");
                sort = Sort.by(Sort.Direction.DESC, "createdTime");
            }
            case 4 -> {
                if (start == null || end == null) throw new IllegalArgumentException("start와 end 날짜는 필수입니다.");
                sort = Sort.by(Sort.Direction.ASC, "createdTime");
            }
            default -> Sort.by(Sort.Direction.ASC, "likecounts");
        }
        if (sort == null) sort = Sort.by(Sort.Direction.DESC, "createdTime");

        Pageable pageable = PageRequest.of(page, 10, sort);

        Page<Board> boards = boardRepository.findAll(pageable);
        return boards.map(BoardResponseDto::new);
    }


    // 개시판 특정 조회
    public BoardResponseDto getBoard(long boardId) {
        Board board = getBoard_long(boardId);
        long likeCount = getLikeCount(boardId);
        return new BoardResponseDto(board, likeCount);
    }


    // 개시판 특정 좋아요
    @Transactional
    public BoardResponseDto getBoardLike(HttpServletRequest servletRequest, long boardId) {
        Board board = getBoard_long(boardId);
        User user = jwt.getTokenUser(servletRequest);

        if (contentsLikeRepository.existsByUser_IdAndLikeContentsAndContents(user.getId(), LikeContents.BOARD , board.getId())) {
            throw new IllegalArgumentException("이미 좋아요를 눌렀습니다");
        }

        ContentsLike contentsLike = new ContentsLike(user, board);
        user.getContentsLikeList().add(contentsLike);
        contentsLikeRepository.save(contentsLike);
        long likeCount = getLikeCount(boardId);
        board.setLikecounts(likeCount);
        String like_m = "좋아요를 누르셨습니다.";
        return new BoardResponseDto(board, likeCount, like_m);
    }

    // 개시판 특정 좋아요 삭제
    @Transactional
    public BoardResponseDto getBoardNolike(HttpServletRequest servletRequest, long boardId) {
        Board board = getBoard_long(boardId);
        User user = jwt.getTokenUser(servletRequest);

        if (!contentsLikeRepository.existsByUser_IdAndLikeContentsAndContents(user.getId(), LikeContents.BOARD , board.getId())) {
            throw new IllegalArgumentException("좋아요를 안눌렀습니다");
        }

        ContentsLike contentsLike = contentsLikeRepository.findByUserAndContents(user, board.getId());
        contentsLikeRepository.delete(contentsLike);
        long likeCount = getLikeCount(boardId);
        board.setLikecounts(likeCount);
        String like_m = "좋아요가 취소되었습니다.";
        return new BoardResponseDto(board, likeCount, like_m);
    }

    // 개시판 삭제
    @Transactional
    public String deleteBoard(HttpServletRequest servletRequest, BoardRequestDto boardRequestDto) {
        Board board = getStringBoard(servletRequest, boardRequestDto);
        boardRepository.delete(board);
        return "삭제 완료";
    }

    // 개시판 수정
    @Transactional
    public String updateBoard(HttpServletRequest servletRequest, BoardRequestDto boardRequestDto) {
        Board board = getStringBoard(servletRequest, boardRequestDto);
        board.update(boardRequestDto);
        return "수정완료";
    }


    // 개시판 + 파일 업데이트
    @Transactional
    public String updateMBoard(HttpServletRequest servletRequest, MultipartFile image, MultipartFile movie, String board) {
        try {
            User user = jwt.getTokenUser(servletRequest);
            Board new_board = getIdBoard(getStringBoard(board));
            if (!user.getId().equals(new_board.getUser_id())) {
                throw new IllegalArgumentException("소유한 개시판이 아닙니다");
            }

            new_board.update(getStringBoard(board));
            Optional<Multimedia> multimediaOptional = multimediaRepository.findById(new_board.getId());
            if (multimediaOptional.isEmpty()) throw new IllegalArgumentException("삽입된 멀티미디어가 없습니다");

            Multimedia multimedia = multimediaOptional.get();
            multimedia.setBoard(new_board);

            if (image != null && !image.isEmpty() && image.getContentType() != null && image.getContentType().toLowerCase().contains("image")) {
                String imageKey = "images/" + UUID.randomUUID();
                uploadFileToS3(imageKey, image.getBytes(), image.getContentType());
                multimedia.setImageUrl(getS3Url(imageKey));
            }

            if (movie != null && !movie.isEmpty() && movie.getContentType() != null && (movie.getContentType().toLowerCase().contains("mp4") || movie.getContentType().toLowerCase().contains("avi"))) {
                String movieKey = "movies/" + UUID.randomUUID();
                uploadFileToS3(movieKey, movie.getBytes(), movie.getContentType());
                multimedia.setMovieUrl(getS3Url(movieKey));
            }

            multimediaRepository.save(multimedia);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "수정 완료";
    }


    //:::::::::::::::::::/* 도구 상자 */:::::::::::::::::::


    //개시판 유저 체크
    public Board getStringBoard(HttpServletRequest servletRequest, BoardRequestDto boardRequestDto) {
        User user = jwt.getTokenUser(servletRequest);
        Board board = getIdBoard(boardRequestDto);
        if (!user.getId().equals(board.getUser().getId())) {
            throw new IllegalArgumentException("개시판의 소유자가 아닙니다.");
        }
        return board;
    }

    // 문자열 Board로 변환
    public BoardRequestDto getStringBoard(String board) throws JsonProcessingException {
        return objectMapper.readValue(board, BoardRequestDto.class);
    }

    // 개시판 id로 찾아서 가셔오기

    public Board getIdBoard(BoardRequestDto boardRequestDto) {
        Optional<Board> boards = boardRepository.findById(boardRequestDto.getId());
        if (boards.isEmpty()) throw new NullPointerException("사용자의 개시물이 없습니다.");
        return boards.get();
    }

    //long값을 이용한 유져 가져오기
    private Board getBoard_long(long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException(boardId + "번의 개시판은 없습니다"));

        return board;
    }

    // 좋아요 숫자
    private long getLikeCount(long boardId) {
        long likeCount = contentsLikeRepository.countByLikeContentsAndContents(LikeContents.BOARD, boardId);
        return likeCount;
    }
}