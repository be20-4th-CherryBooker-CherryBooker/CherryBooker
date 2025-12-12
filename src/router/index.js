import { createRouter, createWebHistory } from "vue-router";
import {useAuthStore} from "@/stores/AuthStore.js";

const routes = [
    {
        path: "/",
        name: "home",
        component: () => import("../views/home/HomeView.vue")
    },
    {
        path: "/oauth2/success",
        name: "oauthSuccess",
        component: () => import("../views/login/OAuthSuccessView.vue"),
        meta: { hideNav: true }
    },
    {
        path: "/login",
        name: "login",
        component: () => import("../views/login/LoginView.vue"),
        meta: { hideNav: true } // nav 숨기기
    },
    {
        path: "/admin/login",
        name: "adminLogin",
        component: () => import("../views/login/AdminLoginView.vue"),
        meta: { hideNav: true } // nav 숨기기
    },
    {
        path: "/mypage",
        name: "mypage",
        component: () => import("../views/mypage/MyPageView.vue")
    },
    {
        path: "/mypage/profile",
        name: "myprofile",
        component: () => import("../views/mypage/MyProfileView.vue")
    },
    {
        path: "/mylibrary",
        name: "mylibrary",
        component: () => import("../views/mylib/MyLibraryView.vue")
    },
    {
        path: "/quotes",
        name: "quotes",
        component: () => import("../views/quote/QuotesView.vue")
    },
    {
        path: "/thread",
        name: "threadList",
        component: () => import("@/views/thread/ThreadListView.vue"),
    },
    {
        path: "/thread/:threadId",
        name: "threadDetail",
        component: () => import("@/views/thread/ThreadDetailView.vue"),
    },

    {
        path: '/admin',
        meta: { role: 'admin' },
        children: [
            {
                path: 'alarms',
                name: 'AdminAlarmTemplates',
                component: () => import('@/views/notification/AlarmTemplateList.vue'),
            },
            {
                path: 'alarms/new',
                name: 'AdminAlarmTemplateCreate',
                component: () => import('@/views/notification/AlarmTemplateCreate.vue'),
            },
            {
                path: 'alarms/:templateId/edit',
                name: 'AdminAlarmTemplateEdit',
                component: () => import('@/views/notification/AlarmTemplateEdit.vue'),
            },
            {
                path: 'alarms/:templateId',
                name: 'AdminAlarmTemplateDetail',
                component: () => import('@/views/notification/AlarmTemplateDetail.vue'),
            },
            {
                path: 'alarms/send',
                name: 'AdminAlarmSendList',
                component: () => import('@/views/notification/AlarmSendList.vue'),
            },
            {
                path: 'alarms/send/create',
                name: 'AdminAlarmSendCreate',
                component: () => import('@/views/notification/AlarmSendCreate.vue'),
            },
        ],
        name: "thread",
        component: () => import("@/views/thread/ThreadView.vue"),
    },
    {
        path: "/admin/reports",
        name: "adminReports",
        component: () => import("../views/admin/report/AdminReportView.vue"),
        meta: { adminNav: true }
    },
    {
        path: "/admin/reports/:reportId",
        name: "adminReportDetail",
        component: () => import("../views/admin/report/AdminReportDetailView.vue"),
        props: true,
        meta: { adminNav: true }
    }

];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach(async (to, from, next) => {
    // Pinia가 초기화된 후에 store import
    const { useAuthStore } = await import('@/stores/AuthStore.js');
    const authStore = useAuthStore();

    // 1. 페이지가 로드될 때 LocalStorage의 토큰으로 상태 복원 및 유효성 검증
    if (localStorage.getItem('accessToken')) {
        authStore.loadFromStorage();
        await authStore.validateToken();
    }

    const isLoggedIn = authStore.isAuthenticated;
    const isAdmin = authStore.isAdmin;

    // 2. OAuth 처리 중이거나 로그인/관리자 로그인 페이지는 예외
    const authPages = ['login', 'adminLogin', 'oauthSuccess'];
    if (authPages.includes(to.name)) {
        // 이미 로그인한 상태에서 로그인 페이지 접근 시 홈으로 (관리자도 일단 home 추후 admin/dashboard로 변경 예정)
        if (isLoggedIn && (to.name === 'login' || to.name === 'adminLogin')) {
            next('/');
            return;
        }
        next();
        return;
    }

    if (to.path.startsWith('/admin')) {
        next();
        return;
    }

    // 3. 로그인하지 않은 상태에서 접근 시 로그인으로
    if (!isLoggedIn) {
        next({
            name: 'login',
            query: { redirect: to.fullPath }
        });
        return;
    }


    // 4. 관리자 페이지 접근 권한 체크 (나중에 admin 페이지 생기면 활성화)
    // if (to.meta.requiresAdmin && !isAdmin) {
    //     alert('관리자 권한이 필요합니다.');
    //     next('/home');
    //     return;
    // }

    // 5. 통과
    next();
});


export default router;
