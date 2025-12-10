import { createRouter, createWebHistory } from "vue-router";

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
        name: "thread",
        component: () => import("@/views/thread/ThreadView.vue"),
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;
