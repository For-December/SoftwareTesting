## 注意
如果您使用idea的配置进行测试，请注意将工作目录设置为`StExp03`,以免路径出错
// ControlFlowNodeParser内的CfgNodeVisitor维护了一个静态计数器
// 重复调用会导致nodeId一直递增