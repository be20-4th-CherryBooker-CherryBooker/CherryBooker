import api from '@/axios';

/**
 * 내 프로필 조회
 * @returns {Promise}
 */
export const getMyProfileApi = async () => {
    const response = await api.get('/api/users/user/me');
    return response.data;
};

/**
 * 마이페이지 조회
 * @returns {Promise}
 */
export const getMyPageInfoApi = async () => {
    const response = await api.get('/api/users/user/mypage');
    return response.data;
};

/**
 * 닉네임 수정
 * @param {string} nickname - 새로운 닉네임
 * @returns {Promise}
 */
export const updateNicknameApi = async (nickname) => {
    const response = await api.patch('/api/users/user', {
        nickname: nickname
    });
    return response.data;
};

/**
 * 회원 탈퇴
 * @returns {Promise}
 */
export const withdrawApi = async () => {
    const response = await api.delete('/api/users/user');
    return response.data;
};