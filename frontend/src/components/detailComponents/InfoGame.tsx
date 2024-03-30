import React from 'react';
import { useNavigate } from 'react-router-dom'; // useNavigate 훅 추가
import { motion } from 'framer-motion';

import styles from './InfoGame.module.css';

import { Game } from '../../stores/DetailStore';
import GameCard from '../commonUseComponents/GameCard';
import useStoreMain from '../../stores/mainStore';

interface InfoGameProps {
    relatedGameList: Game[] | undefined;
    isGame: boolean;
  }

  
const InfoGame: React.FC<InfoGameProps> = ({ relatedGameList, isGame }) => {

    const navigate = useNavigate(); // useNavigate 인스턴스화
    const { data } = useStoreMain(); // useStoreMain 훅을 이용해 데이터 가져오기

    // 관련 게임 또는 인기 게임 데이터 가져오기
    const gamesToShow = (data?.result)?.slice(0, 4); // 최대 5개의 게임만 선택

return (
<>
<div className={styles.container}>
    <div className={styles.title}>{isGame ? '연관 게임' : '인기 게임'}</div>
    <div className="flex justify-center"> {/* 중앙 정렬을 위한 flex 컨테이너 */}
        <motion.ul className="grid gap-4 grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4"
        variants={{
            hidden: {},
            visible: { transition: { staggerChildren: 0.1 } }
        }}
        initial="hidden"
        animate="visible"
        >        {/* relatedGameList가 존재하고, isGame이 true일 때만 게임 카드를 렌더링 */}
        {(relatedGameList ? relatedGameList : gamesToShow)?.map((game, index) => (
            <motion.li key={index} className="list-none"
            variants={{
                hidden: { x: -60, opacity: 0 },
                visible: { x: 0, opacity: 1, transition: { duration: 0.1 } }
            }}
            >
            <GameCard
                key={index}
                gameId={game.gameId}
                imageUrl={game.gameHeaderImg}
                title={game.gameName}
                price={`₩ ${game.gamePriceFinal}`}
                tagsAll={game.tagList}
                tags={game.tagList.filter(tag => tag.codeId === "GEN").map(tag => tag.tagName)}
                likes={game.gameLike}
                // isPrefer={game.isPrefer}
                onGameClick={(gameId) => navigate(`/detail/${gameId}`)} // 클릭 이벤트 처리
            />
            </motion.li>
        ))}
        </motion.ul>
    </div>
    </div>
</>
);
};

export default InfoGame;
