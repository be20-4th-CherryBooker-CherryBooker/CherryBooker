// src/assets/icon/icons.js

// 40x40 카드형 아이콘 (네모 + 종)
import iconNotificationCard from '@/assets/icon/icon-headbar-alert.svg'

// 필요하면 나중에 진짜 hover 버전도 추가 가능
// import iconNotificationCardHover from '@/assets/icon/icon-headbar-alert-hover.svg'

export const icons = {
    notification: {
        default: iconNotificationCard,
        // hover 파일이 따로 없으면 default를 재사용
        hover: iconNotificationCard,
    },
}
