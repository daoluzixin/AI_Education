<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

if (userStore.isLoggedIn && userStore.role) {
  router.replace(`/${userStore.role}`)
}
</script>

<template>
  <div class="home-page min-h-screen">

    <!-- 背景装饰层 -->
    <div class="bg-layer">
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
      <div class="bg-grain"></div>
    </div>

    <!-- 导航栏 -->
    <nav class="home-nav flex items-center justify-between">
      <div class="flex items-center gap-3">
        <div class="nav-logo">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white"
               stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
          </svg>
        </div>
        <span class="nav-brand">西安家教通</span>
      </div>
      <router-link to="/login" class="nav-login-btn">
        立即登录
      </router-link>
    </nav>

    <!-- Hero 区域 -->
    <section class="home-hero text-center">
      <!-- 学术装饰 SVG -->
      <div class="hero-decor">
        <svg class="decor-book" width="64" height="64" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
          <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
          <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
        </svg>
        <svg class="decor-cap" width="56" height="56" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 10v6M2 10l10-5 10 5-10 5z"/>
          <path d="M6 12v5c3 3 6 3 6 3s3 0 6-3v-5"/>
        </svg>
      </div>

      <p class="hero-badge">西安本地 · 大学生家教平台</p>
      <h1 class="hero-title">
        让每个孩子<br>
        <span class="hero-highlight">都能找到适合的</span>大学生家教
      </h1>
      <p class="hero-subtitle">
        连接优质大学生教师与有需求的家庭，让孩子的成长之路有更好的陪伴
      </p>
      <router-link to="/login" class="hero-btn">
        开始使用
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
             stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M5 12h14M12 5l7 7-7 7"/>
        </svg>
      </router-link>
    </section>

    <!-- 分隔装饰线 -->
    <div class="divider">
      <div class="divider-line"></div>
      <div class="divider-dot"></div>
      <div class="divider-line"></div>
    </div>

    <!-- 三端角色介绍 -->
    <section class="home-section">
      <div class="home-container">
        <div class="role-grid">
          <div class="role-card">
            <div class="role-icon role-icon-parent">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </div>
            <h3 class="role-title">家长端</h3>
            <p class="role-desc">发布辅导需求，按科目、区域、价格精准筛选优质大学生家教老师</p>
          </div>

          <div class="role-card">
            <div class="role-icon role-icon-teacher">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 10v6M2 10l10-5 10 5-10 5z"/>
                <path d="M6 12v5c3 3 6 3 6 3s3 0 6-3v-5"/>
              </svg>
            </div>
            <h3 class="role-title">教师端</h3>
            <p class="role-desc">提交个人资料与教学经验，通过资质审核后即可匹配家教机会</p>
          </div>

          <div class="role-card">
            <div class="role-icon role-icon-admin">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              </svg>
            </div>
            <h3 class="role-title">管理后台</h3>
            <p class="role-desc">审核教师资质，维护平台秩序，保障教学质量</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 平台特色 -->
    <section class="home-section" style="padding-bottom: 48px">
      <div class="home-container">
        <h2 class="section-title">平台特色</h2>
        <div class="feature-grid">
          <div class="feature-card">
            <div class="feature-icon-wrap">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                <path d="M9 12l2 2 4-4"/>
              </svg>
            </div>
            <p class="feature-name">实名认证</p>
            <p class="feature-desc">身份信息严格审核</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon-wrap">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 10v6M2 10l10-5 10 5-10 5z"/>
                <path d="M6 12v5c3 3 6 3 6 3s3 0 6-3v-5"/>
              </svg>
            </div>
            <p class="feature-name">学籍审核</p>
            <p class="feature-desc">确认在校大学生身份</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon-wrap">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                <circle cx="12" cy="10" r="3"/>
              </svg>
            </div>
            <p class="feature-name">就近匹配</p>
            <p class="feature-desc">按区域精准推荐老师</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon-wrap">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
                <polyline points="10 9 9 9 8 9"/>
              </svg>
            </div>
            <p class="feature-name">需求发布</p>
            <p class="feature-desc">一键发布找到合适家教</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 底部 -->
    <footer class="home-footer">
      <div class="footer-line"></div>
      <p>西安家教通 · 大学生家教供需匹配平台</p>
    </footer>
  </div>
</template>

<style scoped>
a { text-decoration: none; }

/* ======== 整体背景 ======== */
.home-page {
  position: relative;
  overflow: hidden;
  background: linear-gradient(170deg, #FAF6F0 0%, #F5EDE4 25%, #F0EBE3 50%, #EDE8E0 75%, #F8F4EE 100%);
  color: #3D3427;
}

/* 背景装饰 */
.bg-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}
.bg-orb-1 {
  width: 500px; height: 500px;
  top: -100px; right: -120px;
  background: radial-gradient(circle, #E8D5B8 0%, transparent 70%);
}
.bg-orb-2 {
  width: 400px; height: 400px;
  top: 40%; left: -100px;
  background: radial-gradient(circle, #D4C4A8 0%, transparent 70%);
  opacity: 0.35;
}
.bg-orb-3 {
  width: 350px; height: 350px;
  bottom: -50px; right: 20%;
  background: radial-gradient(circle, #C9B896 0%, transparent 70%);
  opacity: 0.3;
}
/* 纸质纹理 */
.bg-grain {
  position: absolute;
  inset: 0;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  background-size: 256px;
}

/* ======== 导航 ======== */
.home-nav {
  position: relative;
  z-index: 10;
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 32px;
}
.nav-logo {
  width: 42px; height: 42px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #8B734A, #A6905C);
  box-shadow: 0 2px 8px rgba(139,115,74,0.3);
}
.nav-brand {
  font-size: 18px;
  font-weight: 700;
  color: #3D3427;
  letter-spacing: 0.5px;
}
.nav-login-btn {
  padding: 8px 22px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #FFF;
  background: linear-gradient(135deg, #8B734A, #A6905C);
  box-shadow: 0 2px 10px rgba(139,115,74,0.25);
  transition: all 0.2s ease;
}
.nav-login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(139,115,74,0.35);
}

/* ======== Hero ======== */
.home-hero {
  position: relative;
  z-index: 10;
  max-width: 720px;
  margin: 0 auto;
  padding: 48px 24px 72px;
}
.hero-decor {
  pointer-events: none;
  position: absolute;
  inset: 0;
}
.decor-book {
  position: absolute;
  top: 10px; left: -40px;
  color: #C9B896;
  opacity: 0.2;
  transform: rotate(-12deg);
}
.decor-cap {
  position: absolute;
  top: 0; right: -30px;
  color: #B8A580;
  opacity: 0.18;
  transform: rotate(8deg);
}
.hero-badge {
  display: inline-block;
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 1.5px;
  color: #8B734A;
  background: rgba(139,115,74,0.08);
  border: 1px solid rgba(139,115,74,0.15);
  margin-bottom: 28px;
}
.hero-title {
  font-size: 44px;
  font-weight: 800;
  line-height: 1.35;
  color: #2C2418;
  margin-bottom: 20px;
  letter-spacing: -0.5px;
}
.hero-highlight {
  background: linear-gradient(135deg, #8B734A, #B8A060);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-subtitle {
  font-size: 16px;
  line-height: 1.8;
  color: #7A6E5D;
  max-width: 480px;
  margin: 0 auto 36px;
}
.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 36px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  color: #FFF;
  background: linear-gradient(135deg, #7A6340, #9E8A5E);
  box-shadow: 0 4px 20px rgba(122,99,64,0.3), 0 1px 3px rgba(0,0,0,0.06);
  transition: all 0.25s ease;
}
.hero-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(122,99,64,0.4);
}

/* ======== 分隔线 ======== */
.divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 0 24px 56px;
  position: relative;
  z-index: 10;
}
.divider-line {
  width: 80px;
  height: 1px;
  background: linear-gradient(90deg, transparent, #C9B896, transparent);
}
.divider-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #C9B896;
}

/* ======== 通用区块 ======== */
.home-section {
  padding: 0 24px 56px;
  position: relative;
  z-index: 10;
}
.home-container {
  max-width: 960px;
  margin: 0 auto;
}
.section-title {
  text-align: center;
  font-size: 20px;
  font-weight: 700;
  color: #3D3427;
  margin-bottom: 28px;
  letter-spacing: 0.5px;
}

/* ======== 三端角色卡片 ======== */
.role-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.role-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 40px 24px 36px;
  border-radius: 20px;
  background: rgba(255,255,255,0.6);
  border: 1px solid rgba(201,184,150,0.2);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}
.role-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(139,115,74,0.1);
  background: rgba(255,255,255,0.8);
}
.role-icon {
  width: 56px; height: 56px;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 20px;
}
.role-icon-parent {
  background: linear-gradient(135deg, #E8DDD0, #D4C8B8);
  color: #6B5B45;
}
.role-icon-teacher {
  background: linear-gradient(135deg, #D8E8D0, #C4D8B8);
  color: #4A6B3A;
}
.role-icon-admin {
  background: linear-gradient(135deg, #E8DCC8, #DACED0);
  color: #7A6840;
}
.role-title {
  font-size: 17px;
  font-weight: 700;
  color: #2C2418;
  margin-bottom: 10px;
}
.role-desc {
  font-size: 13.5px;
  line-height: 1.75;
  color: #7A6E5D;
  max-width: 220px;
}

/* ======== 平台特色卡片 ======== */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 32px 16px 28px;
  cursor: default;
  border-radius: 16px;
  background: rgba(255,255,255,0.5);
  border: 1px solid rgba(201,184,150,0.15);
  backdrop-filter: blur(8px);
  transition: all 0.3s ease;
}
.feature-card:hover {
  background: rgba(255,255,255,0.75);
  transform: translateY(-2px);
}
.feature-icon-wrap {
  width: 48px; height: 48px;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, rgba(139,115,74,0.1), rgba(166,144,92,0.08));
  color: #8B734A;
  margin-bottom: 14px;
}
.feature-name {
  font-size: 14px;
  font-weight: 600;
  color: #3D3427;
  margin-bottom: 4px;
}
.feature-desc {
  font-size: 12px;
  color: #9A8E7E;
}

/* ======== 底部 ======== */
.home-footer {
  position: relative;
  z-index: 10;
  text-align: center;
  padding: 24px 24px 40px;
}
.footer-line {
  width: 48px;
  height: 1px;
  background: #C9B896;
  margin: 0 auto 16px;
  opacity: 0.5;
}
.home-footer p {
  font-size: 13px;
  color: #9A8E7E;
  letter-spacing: 1px;
}

/* ======== 移动端适配 ======== */
@media (max-width: 768px) {
  .hero-title { font-size: 30px; }
  .home-hero { padding: 36px 20px 48px; }
  .role-grid { grid-template-columns: 1fr; gap: 14px; }
  .feature-grid { grid-template-columns: repeat(2, 1fr); }
  .decor-book, .decor-cap { display: none; }
}
</style>
