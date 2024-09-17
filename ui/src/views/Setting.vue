<script setup lang="ts">
import { axiosInstance } from "@halo-dev/api-client"
import { ref } from 'vue'
import {VAlert} from "@halo-dev/components";

const msg = ref('')

const submitted = ref(false)
const submitHandler = async () => {
  // Let's pretend this is an ajax request:
  await new Promise((r) => setTimeout(r, 1000))
  console.log(settingData)
  submitted.value = true
  msg.value = '保存成功'
}

let settingData = {
  appid: '',
  secret: '',
  fbcz: '2'
};

</script>

<template>
  <div class="setting">
    <FormKit
      type="form"
      submit-label="save"
      @submit="submitHandler"
      :actions="false"
      v-model="settingData"
      @submit-invalid="submitted = false"
    >
      <h1 class="text-2xl font-bold mb-2">微信公众号文章推送设置</h1>
      <FormKit
        type="text"
        name="appid"
        label="appid"
        validation="required"
      />
      <FormKit
        type="text"
        name="secret"
        label="secret"
        validation="required"
      />
      <FormKit
        type="select"
        name="fbcz"
        label="【发布到微信公众号】按钮的实际操作"
        validation="required"
        :options="{1:'保存到草稿', 2: '直接发布'}"
      />
      <FormKit type="submit" label="保存" />
    </FormKit>
    <div v-if="submitted && msg != ''">
      <VAlert v-bind="{type:'default',title:'default',description:msg}" @close="msg = ''"/>
    </div>
  </div>
</template>

<style scoped lang="scss">
.setting {
  margin: 10px;
  padding: 10px;
  border: 2px;
  border-radius: 2px;
  background-color: white;
}
</style>
