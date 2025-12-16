<!-- src/components/thread/CommentItem.vue -->
<template>
  <div class="comment-wrapper">
    <div class="comment-header">
      <img src="/images/user.png" class="avatar" />
      <span>{{ comment.userName }}</span>
      <span class="date">{{ comment.createdAt }}</span>
    </div>

    <div class="comment-content">
      {{ comment.content }}
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  comment: Object
});
</script>

<style scoped>
.comment-wrapper {
  background: #fff9ec;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 8px;
}

.comment-header {
  display: flex;
  gap: 6px;
  align-items: center;
}

.avatar {
  width: 20px;
  height: 20px;
}

.date {
  font-size: 11px;
  color: #999;
}

.comment-content {
  margin-top: 6px;
  font-size: 14px;
}
</style>
