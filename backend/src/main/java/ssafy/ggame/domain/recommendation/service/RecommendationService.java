package ssafy.ggame.domain.recommendation.service;

import com.querydsl.core.Tuple;
import jdk.javadoc.doclet.Taglet;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ssafy.ggame.domain.code.entity.Code;
import ssafy.ggame.domain.code.repository.CodeRepository;
import ssafy.ggame.domain.game.dto.GameCardDto;
import ssafy.ggame.domain.game.entity.Game;
import ssafy.ggame.domain.game.repository.GameCustomRepository;
import ssafy.ggame.domain.game.repository.GameRepository;
import ssafy.ggame.domain.gameTag.entity.GameTag;
import ssafy.ggame.domain.gameTag.repository.GameTagRepository;
import ssafy.ggame.domain.prefer.entity.Prefer;
import ssafy.ggame.domain.prefer.repository.PreferRepository;
import ssafy.ggame.domain.recommendation.dto.GameIdAndTagDto;
import ssafy.ggame.domain.recommendation.dto.RecommendationResponseDto;
import ssafy.ggame.domain.recommendation.dto.SearchGameRequestDto;
import ssafy.ggame.domain.recommendation.dto.TempDto;
import ssafy.ggame.domain.tag.dto.TagDto;
import ssafy.ggame.domain.tag.entity.Tag;
import ssafy.ggame.domain.tag.repository.TagRepository;
import ssafy.ggame.domain.user.entity.User;
import ssafy.ggame.domain.user.repository.UserRepository;
import ssafy.ggame.domain.userTag.dto.UserTagDto;
import ssafy.ggame.domain.userTag.entity.UserTag;
import ssafy.ggame.domain.userTag.repository.UserTagCustomRepository;
import ssafy.ggame.domain.userTag.repository.UserTagRepository;
import ssafy.ggame.global.common.StatusCode;
import ssafy.ggame.global.exception.BaseException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserRepository userRepository;
    private final CodeRepository codeRepository;
    private final TagRepository tagRepository;
    private final GameRepository gameRepository;
    private final GameTagRepository gameTagRepository;
    private final PreferRepository preferRepository;
    private final UserTagRepository userTagRepository;
    private final GameCustomRepository gameCustomRepository;
    private final UserTagCustomRepository userTagCustomRepository;

    public List<GameCardDto> getPopularList(Integer userId, String codeId, Short tagId, int page, int size) {
        List<GameCardDto> gameCardDtoList = null;
        // 전체 게임 인기 순위
        // 로그인 전
        if (userId == 0) {
            // codeId, tagId에 따라 인기게임 가져오기
            gameCardDtoList = getGameCardList(codeId, tagId, page, size);
        }
        // 로그인 후
        else {
            // 사용자가 존재하지 않을 때 예외 발생
            User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(StatusCode.USER_NOT_FOUND));

            // codeId, tagId에 따라 인기게임 가져오기
            gameCardDtoList = getGameCardList(codeId, tagId, page, size);

            // userId에 따라  isPrefer 가져와 업데이트 하기
            // - 유저가 선호하는 게임 아이디 목록 가져오기
            List<Prefer> preferList = preferRepository.findByUserId(userId);
            HashSet<Long> preferGameIdSet = new HashSet<>();
            for (Prefer prefer : preferList) {
                preferGameIdSet.add(prefer.getPreferId().getGame().getGameId());
            }

            // - 인기게임에 선호하는 게임이 포함되어 있으면 인기게임의 isprefer를 true로 지정
            for (GameCardDto gameCardDto : gameCardDtoList) {
                if (preferGameIdSet.contains(gameCardDto.getGameId())) {
                    gameCardDto.updateIsPrefer(true);
                }
            }
        }
        return gameCardDtoList;
    }

    private List<GameCardDto> getGameCardList(String codeId, Short tagId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        List<GameCardDto> gameCardDtoList = new ArrayList<>();
        // codeId, tagId에 따른 gameCardList 만들기
        // codeId 가 없을 때,
        Optional<Code> optionalCode = codeRepository.findByCodeId(codeId);
        if (!codeId.equals("0") && optionalCode.isEmpty()) {
            // 해당 코드가 존재하지 않는다는 예외 발생
            throw new BaseException(StatusCode.CODE_NOT_EXIST);
        }
        // tagId가 없을 때,
        Optional<Tag> optionalTag = tagRepository.findByCodeIdAndTagId(codeId, tagId);
        if (!codeId.equals("0") && optionalTag.isEmpty()) {
            // 해당 태그가 존재하지 않는다는 예외 발생
            throw new BaseException(StatusCode.TAG_NOT_EXIST);
        }

        // codeId, tagId가 둘 다 0일떄
        if (codeId.equals("0") && tagId == 0) {
            List<Game> gameList = gameRepository.findAllByOrderByGameFinalScore(pageable);
            gameCardDtoList = makeGameCardDtoList(gameList);
        }

        // codeId, tagId 둘 다 0이 아닐 때
        if (!codeId.equals("0") && tagId != 0) {
            // game을 인기순으로 가져온다
            // TODO: 1. game 가져와서 TempDto 적용해서 gameCardDto 만들기
            // `모든 게임 아이디 받아오기

            // 해당 태그를 갖고 있는 모든 게임의 아이디 가져오기
            List<Long> gameIdList = gameTagRepository.findAllGameIdByCodeIdAndTagId(codeId, tagId);
            // 게임카드디티오를 만들기위해 필요한 정보를 게임 아이디를 통해 가져오기(게임 가치점수로 정렬됨)
            Page<TempDto> gameTempDtoList = gameCustomRepository.findAllGameAndTagList(gameIdList, pageable);

            gameCardDtoList = new ArrayList<>();
            //tempDto -> convertToGameCard
            for(TempDto tempDto : gameTempDtoList){
                gameCardDtoList.add(tempDto.converToGameCardDto());
            }

            // pageable 적용하기


//            List<Game> gameList = gameRepository.findAllByOrderByGameFinalScore(pageable);
//            // 거기서 코드아이디(gen), tagId 로 필터링 한다.
//            // - 입력으로 받은 게임태그 가져오기
////            Tag gameTag = tagRepository.findByCodeIdAndTagId(codeId, tagId).orElseThrow(()->new BaseException(StatusCode.TAG_NOT_EXIST));
//            List<Game> filteredGameList = new ArrayList<>();
//            for (Game game : gameList) {
//                // - 만약 게임이 해당 게임 태그를 가졌다면 걸러진 게임 목록에 추가
//                GameTag gameTag = gameTagRepository.findByCodeIdAndTagId(codeId, tagId);
//                if (game.getGameTags().contains(gameTag)) {
//                    filteredGameList.add(game);
//                }
//            }
//            gameCardDtoList = makeGameCardDtoList(filteredGameList);
        }

        return gameCardDtoList;
    }

    public List<GameCardDto> makeGameCardDtoList(List<Game> gameList) {
        List<GameCardDto> gameCardDtoList = new ArrayList<>();

        // 받은 게임 별 총 좋아요 수 맵
        List<Long> ids = new ArrayList<>();
        gameList.forEach((g) -> ids.add(g.getGameId()));
        Map<Long, Long> likesMap = gameCustomRepository.getLikes(ids);


        for (Game game : gameList) {
            GameCardDto gameCardDto = game.converToGameCardDto();
            //tagList 업데이트
            List<TagDto> tagDtoList = new ArrayList<>();
            for (GameTag tag : game.getGameTags()) {
                tagDtoList.add(tag.convertToTagDto());
            }
            gameCardDto.updateTagList(tagDtoList);
            gameCardDto.updateLike(likesMap.getOrDefault(game.getGameId(), 0L));
            gameCardDtoList.add(gameCardDto);
        }
        return gameCardDtoList;
    }

    public RecommendationResponseDto getPersonalList(Integer userId) {
        // 사용자 존재 유무 확인
        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(StatusCode.USER_NOT_FOUND));

        // 1. 사용자 가중치 전부 가져오기
        List<UserTagDto> userTagList = userTagCustomRepository.findByUserId(user.getUserId());

        // 2. 가져온 태그 - 사용자 가중치 맵으로 만들기 (TagDto - weight)
        Map<TagDto, Long> tagWeightMap = new HashMap<>();
        for (UserTagDto userTag : userTagList) {
            TagDto tagDto = TagDto.builder()
                    .codeId(userTag.getCodeId())
                    .tagId(userTag.getTagId())
                    .tagName(userTag.getTagName())
                    .build();
            tagWeightMap.put(tagDto, Long.valueOf(userTag.getUserTagWeight()));
        }

        System.out.println("tagWeightMap = " + tagWeightMap);

        // 3. 입력으로 받은 태그를 빈도수 별로 정렬해서 결과로 반환
        // - tagWeightMap을 value 내림차순으로 정렬
        List<TagDto> tagDtoList = getSortedTagDtoList(tagWeightMap);

        System.out.println("tagDtoList = " + tagDtoList);

        // 5. 검색 결과에 보여줄 정해진 개수만큼 태그 반환 (9개) - 메인 필터링을 위해
        List<TagDto> resultTagDtoList = tagDtoList.stream()
                .limit(9) // 0부터 8번째 요소까지
                .toList();

        System.out.println("resultTagDtoList = " + resultTagDtoList);

        // 6. 게임별 점수를 저장할 맵 (게임 아이디 - 가중치 합)
        Map<TempDto, Double> gameScoreMap = calculateScore(tagDtoList, tagWeightMap);

        System.out.println("calculateScore Done =====================");
        

        // 점수계산을 마쳤으니 내림차순으로 정렬하고,
        // GameCardDto형식으로 변환해서
        // 개수 잘라 반환하기
        List<Map.Entry<TempDto, Double>> sortedGameScoreList = getSortedGameScoreList(gameScoreMap);

        System.out.println("getSortedGameScoreList Done =========================== ");


        // TODO: 100개 잘라서 가져오도록 고치기
        // 100개만 잘라서 가져오기
        List<Map.Entry<TempDto, Double>> subList = sortedGameScoreList.subList(0, 2);

        System.out.println("subList = " + subList);


        //  TODO: 게임 태그가 하나만 들어가는 문제 해결하기
        System.out.println("################## 문제구간 (sortedGameCardDtoList) - 태그가 왜 하나밖에 안담기니 ###########");

        // 반환형식인 gameCardDto로 변환하기
        List<GameCardDto> gameCardDtoList = sortedGameCardDtoList(userId, subList);


        return RecommendationResponseDto.builder()
                .tagDtoList(resultTagDtoList)
                .gameCardDtoList(gameCardDtoList)
                .build();

    }

    private List<Map.Entry<TempDto, Double>> getSortedGameScoreList(Map<TempDto, Double> gameScoreMap) {
        // treeMap을 List로 변환
        List<Map.Entry<TempDto, Double>> sortedGameScoreList = new ArrayList<>(gameScoreMap.entrySet());

        // 점수(value)로 내림차순 정렬
        Collections.sort(sortedGameScoreList, valueComparator);
        return sortedGameScoreList;
    }

    private Map<TempDto, Double> calculateScore(List<TagDto> tagDtoList, Map<TagDto, Long> tagWeightMap) {
        // 6. 게임별 빈도수 점수 (gameId - 빈도수 점수)
        Map<TempDto, Double> gameScoreMap = new TreeMap<>();

        // 7. 게임 점수 계산을 위해 게임 정보를 담을 집합
        Set<TempDto> containGameList = new HashSet<>();

        List<TempDto> gameList = gameCustomRepository.findAllGameAndTag();

        for (TempDto game : gameList) {
            for (TagDto tagDto : tagDtoList) {
                if (game.getCodeId().equals(tagDto.getCodeId()) && game.getTagId() == tagDto.getTagId()) {
                    containGameList.add(game);
                    gameScoreMap.put(game, gameScoreMap.getOrDefault(game, 0.0) + tagWeightMap.get(tagDto));
                }
            }
        }

        // 점수계산
        for (TempDto game : containGameList) {
            Double score1 = Math.log(gameScoreMap.get(game) + 100) * 0.7;
            Double score2 = game.getGameFinalScore() * 0.3;
            gameScoreMap.put(game, score1 + score2);
        }

        return gameScoreMap;
    }

    private static List<TagDto> getSortedTagDtoList(Map<TagDto, Long> tagWeightMap) {
        // 3. tagWeighMap을 value 내림차순으로 정렬
        ArrayList<Map.Entry<TagDto, Long>> tagWeightList = new ArrayList<>(tagWeightMap.entrySet());
        tagWeightList.sort((e1, e2) -> {
            int compare = e2.getValue().compareTo(e1.getValue()); // 빈도수를 내림차순으로 정렬
            if (compare == 0) { // 빈도수가 같을 때는 tagId 값을 비교하여 오름차순으로 정렬
                return e1.getKey().getTagId().compareTo(e2.getKey().getTagId());
            }
            return compare;
        });

        // 4. List<TagDto> tagDtoList 지정
        List<TagDto> tagDtoList = new ArrayList<>();
        for (Map.Entry<TagDto, Long> tag : tagWeightList) {
            tagDtoList.add(tag.getKey());
        }
        return tagDtoList;
    }


    public RecommendationResponseDto searchGameList(SearchGameRequestDto searchGameRequestDto) {

        // 1. 게임 아이디, 게임 태그 리스트
        List<GameIdAndTagDto> gameIdAndTagDtoList = searchGameRequestDto.getGameIdAndTagDtoList();

        // 2. 담은 게임의 태그별 빈도수 세기(가중치) (TagDTO - 빈도수)
        Map<TagDto, Long> tagCntMap = new HashMap<>();
        for (GameIdAndTagDto gameIdAndTagDto : gameIdAndTagDtoList) {
            for (TagDto tagDto : gameIdAndTagDto.getTagList()) {
                Tag tag = tagRepository.findByCodeIdAndTagId(tagDto.getCodeId(), tagDto.getTagId()).orElseThrow(() -> new BaseException(StatusCode.TAG_NOT_EXIST));
                tagCntMap.put(tag.convertToTagDto(), tagCntMap.getOrDefault(tagDto, 0L) + 1L);
            }
        }

        // 3. 입력으로 받은 태그를 빈도수 별로 정렬해서 결과로 반환
        // - tagCntMap을 value 내림차순으로 정렬
        List<TagDto> tagDtoList = getSortedTagDtoList(tagCntMap);

        // 5. 검색 결과에 보여줄 정해진 개수만큼 태그 반환 (5개)
        List<TagDto> resultTagDtoList = tagDtoList.stream()
                .limit(5) // 0부터 4번째 요소까지
                .collect(Collectors.toList());

        // 6. 게임별 빈도수 점수 (gameId - 빈도수 점수)
        Map<TempDto, Double> gameScoreMap = calculateScore(tagDtoList, tagCntMap);

        // 점수계산을 마쳤으니 내림차순으로 정렬하고,
        // GameCardDto형식으로 변환해서
        // 개수 잘라 반환하기
        List<Map.Entry<TempDto, Double>> sortedGameScoreList = getSortedGameScoreList(gameScoreMap);

        // 15개만 잘라서 가져오기
        List<Map.Entry<TempDto, Double>> subList = sortedGameScoreList.subList(0, 15);

        // 반환형식인 gameCardDto로 변환하기
        Integer userId = searchGameRequestDto.getUserId();
        List<GameCardDto> gameCardDtoList = sortedGameCardDtoList(userId, subList);

        return RecommendationResponseDto.builder()
                .tagDtoList(resultTagDtoList)
                .gameCardDtoList(gameCardDtoList)
                .build();

    }

    private List<GameCardDto> sortedGameCardDtoList(Integer userId, List<Map.Entry<TempDto, Double>> list) {
        List<GameCardDto> gameCardDtoList = new ArrayList<>();

        // 받은 게임 별 총 좋아요 수 맵
        List<Long> ids = new ArrayList<>();
        list.forEach((e) -> ids.add(e.getKey().getGameId()));
        Map<Long, Long> likesMap = gameCustomRepository.getLikes(ids);


        for (Map.Entry<TempDto, Double> entry : list) {
            TempDto game = entry.getKey();
            System.out.println("TempDto - game = " + game);
            GameCardDto gameCardDto = game.converToGameCardDto();
            gameCardDto.updateLike(likesMap.getOrDefault(game.getGameId(), 0L)); // 게임 총 좋아요 수 업데이트
            // 선호 여부 업데이트
            if (preferRepository.existsByPreferId_User_UserIdAndPreferId_Game_GameId(userId, game.getGameId())) {
                gameCardDto.updateIsPrefer(true);
            }
            List<TagDto> tagDtoList = new ArrayList<>();
            tagDtoList.add(TagDto.builder()
                    .codeId(game.getCodeId())
                    .tagId(game.getTagId())
                    .tagName(game.getTagName())
                    .build());

            gameCardDto.updateTagList(tagDtoList);
            gameCardDtoList.add(gameCardDto);
        }
        return gameCardDtoList;
    }

    private Map<Long, Double> calculateGameScore(Map<Long, Double> gameScoreMap) {
        for (Long key : gameScoreMap.keySet()) {
            double score1 = Math.log(gameScoreMap.get(key) + 100) * 0.7;
            Game game = gameRepository.findById(key).orElseThrow(() -> new BaseException(StatusCode.GAME_NOT_FOUND));
            double score2 = game.getGameFinalScore() * 0.3;

            gameScoreMap.put(key, (score1 + score2));
        }
        return gameScoreMap;
    }

    private Comparator<Map.Entry<TempDto, Double>> valueComparator = (e1, e2) -> {
        return e2.getValue().compareTo(e1.getValue()); // 내림차순으로 정렬;
    };

    public List<GameCardDto> getRecentPopularGameList() {
        List<Game> recentTop10 = gameRepository.findFirst10ByOrderByGameFinalRecentScoreDesc();

        System.out.println("recentTop10.size() = " + recentTop10.size());

        List<GameCardDto> gameCardDtoList = new ArrayList<>();


        // 받은 게임 별 총 좋아요 수 맵
        List<Long> ids = new ArrayList<>();
        recentTop10.forEach((g) -> ids.add(g.getGameId()));
        Map<Long, Long> likesMap = gameCustomRepository.getLikes(ids);
        for (Game game : recentTop10) {
            GameCardDto gameCardDto = game.converToGameCardDto();
            gameCardDto.updateLike(likesMap.getOrDefault(game.getGameId(), 0L));
            gameCardDtoList.add(gameCardDto);
        }
        return gameCardDtoList;
    }
}
