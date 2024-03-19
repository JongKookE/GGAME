package ssafy.ggame.domain.recommendation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.ggame.domain.game.dto.GameCardDto;
import ssafy.ggame.domain.game.entity.Game;
import ssafy.ggame.domain.recommendation.repository.RecommendationRepository;
import ssafy.ggame.domain.tag.Code;
import ssafy.ggame.domain.tag.entity.Tag;
import ssafy.ggame.domain.tag.repository.CodeRepository;
import ssafy.ggame.domain.tag.repository.TagRepository;
import ssafy.ggame.domain.user.entity.User;
import ssafy.ggame.domain.user.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final CodeRepository codeRepository;
    private final TagRepository tagRepository;
    public List<GameCardDto> getPopularList(Integer userId, String codeId, Integer tagId) {
        // 전체 게임 인기 순위
        // TODO:
        //      - 로그인 여부에 따라 다르게 처리하기(userId = 0으로 들어오는데, 0인 유저 아이디는 없으므로 어짜피 좋아요한 게임이 없음!!
        //      - isPrefer(), tagList() 채워 넣기
        // 1. user 존재 여부
        //      1-1. 유저가 존재하지 않으면 또는 0이면, 로그인하지 않은것
        // 전체일때
        if(userId == 0){
            // codeId, tagId에 따라 인기게임 가져오기

        }
        // 전체 아닐때
        else{
            Optional<User> optionalUser = userRepository.findById(userId);
            // 사용자가 존재하지 않을 때
            if(optionalUser.isEmpty()){
                // TODO: UserException 발생
            } else{
                User user = optionalUser.get();
                // codeId, tagId에 따라 인기게임 가져오기
                // userId에 따라  isPrefer 가쟈오기

            }
        }
    }


    private List<GameCardDto> getGameCardList(String codeId, Short tagId){
        List<GameCardDto> gameCardDtoList = new ArrayList<>();
        // TODO: codeId, tagId에 따른 gameCardList 만들기
        // codeId 가 없을 때,
        Optional<String> optionalCode = codeRepository.findByCodeId(codeId);
        if(!codeId.equals("0") && optionalCode.isEmpty()){
            // TODO: 해당 코드가 존재하지 않는다는 예외 발생
        }
        // tagId가 없을 때,
        Optional<Tag> optionalTag = tagRepository.findByCodeIdAndTagId(codeId, tagId);
        if(!codeId.equals("0") && optionalTag.isEmpty()){
            // TODO: 해당 태그가 존재하지 않는다는 예외 발생
        }

        // codeId, tagId가 둘 다 0일떄
        if(codeId.equals("0") && tagId == 0){

        }

        // codeId, tagId 둘 다 0이 아닐 때
        return gameCardDtoList;
    }
}
