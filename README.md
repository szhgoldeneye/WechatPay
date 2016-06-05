# WechatPay

微信扫码支付模式demo，功能包含
- 支付
- url转二维码
- 异步通知处理
- 退款

## 业务流程 ##

>业务流程说明：
 
1. 输入对应参数，调用获得code_url的接口生成code_url。

2. 后台系统根据返回的code_url调用接口生成二维码。

3. 用户打开微信“扫一扫”扫描二维码，并支付订单。

4. 微信支付系统通过发送异步消息通知商户后台系统支付结果。商户后台系统需回复接收情况，通知微信后台系统不再发送该单的支付通知。

## 接口参数 ##

| 参数 | 参数说明 | 样例 |
| --- | --- | --- |
|appid|微信分配的公众账号ID|wxd678efh567hg6787|
|mch_id|微信支付分配的商户号|1230000109|
|nonce_str|5K8264ILTKCH16CQ2502SI8ZNMTM67VS|随机字符串，不长于32位|
|body|商品或支付单简要描述|Ipad mini  16G  白色|
|out_trade_no|商户支付的订单号由商户自定义生成，微信支付要求商户订单号保持唯一性（建议根据当前系统时间加随机序列来生成订单号）。重新发起一笔支付要使用原订单号，避免重复支付；已支付过或已调用关单、撤销的订单号不能重新发起支付|20150806125346|
|total_fee|交易金额默认为人民币交易，接口中参数支付金额单位为【分】，参数值不能带小数|100|
|spbill_create_ip|调用微信支付API的机器IP|123.12.12.123|
|notify_url|接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数|http://o2o.cloume.com/tmcp-ms/api/wechat/result|
|trade_type|扫码支付的类型为NATIVE|NATIVE|

更多参数请参考[统一下单API](https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=9_1)
支付结果通知参数请参考[支付结果通用通知](https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=9_7)

## 信息配置 ##

在GlobalConfig配置微信支付的一些信息