package ssafy.ggame.domain.recommendation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.ggame.domain.game.dto.GameCardDto;
import ssafy.ggame.domain.recommendation.dto.GameIdAndTagDto;
import ssafy.ggame.domain.recommendation.dto.SearchGameRequestDto;
import ssafy.ggame.domain.recommendation.service.RecommendationService;
import ssafy.ggame.global.common.BaseResponse;
import ssafy.ggame.global.common.StatusCode;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/popular")
    ResponseEntity<BaseResponse<List<GameCardDto>>> getPopularGameList(
            @RequestParam(required=true, defaultValue = "0") Integer userId,
            @RequestParam(required =true, defaultValue = "0") String codeId,
            @RequestParam(required = true, defaultValue = "0") Short tagId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "100") int size){

        List<GameCardDto> resultList = recommendationService.getPopularList(userId, codeId, tagId, page, size);
        System.out.println("resultList.size() = " + resultList.size());

        return ResponseEntity.ok(new BaseResponse<List<GameCardDto>>(resultList));
    }

    @GetMapping("/personal/{userId}")
    ResponseEntity<BaseResponse<List<GameCardDto>>> getPersonalGameList(@PathVariable Integer userId){
       List<GameCardDto> resultList =  recommendationService.getPersonalList(userId);

       return ResponseEntity.ok(new BaseResponse<List<GameCardDto>>(resultList));
    }


    @PostMapping("/search")
    ResponseEntity<BaseResponse<List<GameCardDto>>> searchGameList(@RequestBody SearchGameRequestDto searchGameRequestDto){
        List<GameCardDto> resultList = recommendationService.searchGameList(searchGameRequestDto);

        return ResponseEntity.ok(new BaseResponse<List<GameCardDto>>(resultList));

    }


}
