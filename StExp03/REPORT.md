## 一些处理
- ControlFlowNodeParser 内的CfgNodeVisitor维护了一个静态计数器
- 重复调用会导致nodeId一直递增
- 三次单测都需要ControlFlowNodeParser给出的控制流程图表，因此添加了个缓存机制
- 

## 提取器类（MyExtractor.java）
MyExtractor.java 是一个提取器类，继承自 BaseExtractor。 

该类提供了三个方法：
- getControlFlowGraphInArray 用于获取控制流图的二维数组表示，
- getTestRequirementsInArray 用于获取基本路径的二维数组表示，
- getTestPathsInArray 用于获取测试路径的二维数组表示。

本次实验的目的是实现这三个方法并通过单测。
## 控制流图生成（CFGGenerator.java）
CFGGenerator.java 负责生成控制流图。

通过调用 ControlFlowNodeParser 的 parseControlFlowNodes 方法，获取控制流节点信息，并将其转换为节点列表。

然后，根据节点的类型和关系，构建控制流图的边列表，并将其转换为二维数组表示。

2.5 节点类（Node.java）

Node.java 定义了节点类， 使用 lombok 的 @Builder 和 @Getter 注解简化代码。

节点类包含节点编号、父节点编号、内容、语句类型和行号等属性。

通过 BuildNode 方法，将解析得到的节点信息字符串转换为节点对象。

2.6 路径生成（PathGenerator.java）

PathGenerator.java 用于生成测试路径。

根据控制流图和基本路径矩阵，构建控制流图的邻接表，并将基本路径转换为列表形式。

通过广度优先搜索（BFS）算法，从指定的起始节点开始，尝试扩展路径，直到到达终止节点。

在扩展路径的过程中，会检查路径中是否存在环，并限制环的迭代次数。

2.7 测试路径生成（TestPathGenerator.java）

TestPathGenerator.java 提供了另一种生成测试路径的方法。

通过深度优先搜索（DFS）算法，根据指定的起点、终点和环的展开次数，生成测试路径。

2.8 基本路径生成（PrimePathGenerator.java）

PrimePathGenerator.java 负责生成基本路径。

通过构建控制流图的邻接表，使用深度优先搜索算法寻找所有可能的环和路径，并移除作为子数组的短路径，最终得到基本路径矩阵。

三、关键算法与实现细节

3.1 控制流图构建

在 CFGGenerator.java 中，通过调用 ControlFlowNodeParser 的 parseControlFlowNodes 方法，获取控制流节点信息。

这里将控制台输出流重定向到一个 ByteArrayOutputStream，以便捕获输出的节点信息。

然后，根据节点的类型（如 for-statement、for-condition 等）和关系，构建控制流图的边列表。

具体来说，对于不同类型的节点，会根据其逻辑关系添加相应的边。

例如，

for-statement 节点指向 for-condition 节点，

for-condition 节点的 true 分支指向 for-body 节点， false 分支指向 after-branch 节点。

3.2 基本路径生成

PrimePathGenerator.java 中，首先构建控制流图的邻接表。

然后，使用深度优先搜索算法寻找所有可能的环和路径。

对于找到的环和路径，会检查是否已经存在相同的路径，避免重复添加。

最后，移除作为子数组的短路径，确保基本路径的独立性。

3.3 测试路径生成

PathGenerator.java 中，使用广度优先搜索算法生成测试路径。

从指定的起始节点开始，尝试扩展路径，直到到达终止节点。

在扩展路径的过程中，会检查路径中是否存在环，并使用 loopCounts 记录环的迭代次数。

如果环的迭代次数超过设定的最大迭代次数，则跳过该路径。

