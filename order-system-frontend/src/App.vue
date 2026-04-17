<template>
  <div style="background:#fff; min-height:100vh;">
    <div style="background:#222; padding:14px; text-align:center;">
      <router-link to="/buyer" style="color:white; margin:0 20px; text-decoration:none;">用户下单</router-link>
      <router-link to="/seller" style="color:white; margin:0 20px; text-decoration:none;">订单管理</router-link>
    </div>
    <router-view />
  </div>
</template>