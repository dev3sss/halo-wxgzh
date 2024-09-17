import {definePlugin} from "@halo-dev/console-shared";
import {IconSettings, VDropdownItem} from "@halo-dev/components";
import {markRaw} from "vue";
import type {ListedPost} from "@halo-dev/api-client";
import Setting from "./views/Setting.vue";
import {axiosInstance} from "@halo-dev/api-client";

const APIS_CONSOLE_URL = '/apis/console.api.wxgzh-helper.mk95.cn/v1'

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/wxgzh-helper",
        name: "WxgzhHelper",
        component: Setting,
        meta: {
          title: "微信公众号助手设置",
          searchable: true,
          menu: {
            name: "微信公众号助手",
            icon: markRaw(IconSettings),
            priority: 0,
          },
        },
      },
    },
  ],
  extensionPoints: {
    "post:list-item:operation:create": () => {
      return [
        {
          priority: 10,
          component: markRaw(VDropdownItem),
          props: {},
          action: (item?: ListedPost) => {
            // do something
            axiosInstance.post(APIS_CONSOLE_URL + '/wx/fb', {
              metadata: {
                // 根据 'xxx-' 前缀自动生成 xxx 的名称作为唯一标识，可以理解为数据库自动生成主键 id
                generateName: "fbInfo-",
              },
              spec: {
                postName: item?.post.metadata.name,
                fbzt: '0'
              },
              // kind: "FbInfo",
              // apiVersion: "wxgzh-helper.mk95.cn/v1",
            }).then(res => {
              // window.location.href = "/console/wxgzh-helper";
              window.open('/md', '_blank');
            })
          },
          label: "发布到微信公众号",
          hidden: false,
          permissions: [],
          children: [],
        }
      ]
    }
  },
});
