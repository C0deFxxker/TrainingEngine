# core模块
存放master模块与worker模块的公共代码，没有打包发布成服务的功能。

包路径解释：
* bean: 非数据库模型。
* dto: 接口交互用的DTO模型。
* dfs: 文件系统接口及实现。
* model: 数据库DO模型。
* mapper: 对应model包下DO模型的Mapper类。
* service: 对应model包下DO模型的Service类。
* configuration: core模块下Bean的自动加载配置。
* Consts类: 存放公共常量。

# master模块
存放master节点的代码，可打包发布成master节点。
依赖于core模块。

# worker模块
存放worker节点的代码，可打包发布成worker节点。
依赖于core模块。