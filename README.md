# Smart Siege (1.20.1 / Forge)

Hostile mobをより賢く・危険にするMod。EpicSiegeMod (funwayguy) のコンセプト（掘る・ドアを壊す・柱を登る・爆発物回避など）を参考に、
1.20.1 Forge用にゼロから実装したオリジナルコードです。
（EpicSiegeMod自体は古いバージョン(1.7〜1.12系)向けのコンパイル済みModで、著作権のあるバイトコードのため
デコンパイル・流用はしていません。）

## 実装済みのAI行動

| 機能 | 説明 | configキー |
|---|---|---|
| 爆発物回避 | 起爆中のTNT・膨張中のクリーパーから逃げる | `enableAvoidExplosions` |
| 矢回避 | 飛んでくる矢を横に避ける | `enableDodgeArrows` |
| 伏兵(待ち伏せ) | 暗い場所でプレイヤーが遠いうちは動かず隠れ、近づくと襲いかかる | `enableAmbush` |
| ドア破壊 | ゾンビ以外の敵Mobも閉じたドアを壊して侵入 | `enableDoorBreaking` |
| 採掘突破 | 通路が塞がれていると柔らかいブロックを掘って追ってくる | `enableDigging` |
| 柱登り | プレイヤーが塔の上に逃げても、ブロックを積んで登ってくる | `enablePillaring` |
| 連携(索敵共有) | 1体がターゲットを見つけると同種の近くの仲間全員に知らせる | `enablePackTactics` |
| 索敵範囲拡張 | followRange属性を拡大し、より遠くから気づく | `enableExpandedSensing` |

すべて `config/smartsiege-server.toml` (ワールド生成後に自動生成) からON/OFF・数値調整が可能です。
`onlyOnHardDifficulty=true` にすると、ワールド難易度がHardの時だけ全機能が有効になります。
`requireMobGriefing=true` (デフォルト) なら、掘る・ドア破壊・柱登りは `mobGriefing` ゲームルールに従います。

## ビルド方法

このリポジトリはForge公式のMDK (ModDeveloperKit) と同じ構成のソースツリーです。ビルドにはインターネット接続と
Forge/Minecraftのライブラリのダウンロードが必要なため、この環境（サンドボックス、ネットワーク遮断）では
コンパイル確認ができていません。お手元の環境で以下の手順を実行してください。

1. Java 17 (JDK) をインストール
2. このフォルダをそのまま開くか、公式 [Forge MDK 1.20.1 (47.3.0)](https://files.minecraftforge.net/) を展開し、
   `src` フォルダとルート直下の `build.gradle` / `gradle.properties` / `settings.gradle` をこのプロジェクトの内容で置き換える
3. ターミナルで:
   ```
   ./gradlew build
   ```
   (Windowsは `gradlew.bat build`)
4. `build/libs/smartsiege-1.0.0.jar` が生成されるので、`mods` フォルダに入れる

初回ビルドはForgeのライブラリ・MCPマッピングをダウンロードするため時間がかかります(数分〜十数分)。

## 既知の制約・注意点

- `Mob.goalSelector` / `targetSelector` はvanilla側でprotectedのため、リフレクション経由でアクセスしています。
  将来のForgeアップデートでフィールド名が変わった場合はここが壊れる可能性があります。
- コンパイルエラーが出た場合は、エラーメッセージを教えてください。実機ビルド環境がないため机上でのレビューですが、
  該当箇所を修正します。
- ボスMob(Ender Dragon, Witherなど)にも適用されるので、難しすぎる場合は今後「ボスは除外」設定を追加できます。
- サーバー(ワールド)側のconfigなので、シングルプレイでも `saves/<world>/serverconfig/smartsiege-server.toml` に生成されます。

## 追加であると良いもの

- **テストワールド**: Peaceful→Easy→Normal→Hardで挙動を比較しながら数値(cooldown, radius, hardness limit等)を調整
- **JEI/クライアント側の表示**: どのMobが「強化AI」持ちか視覚的にわかるパーティクルやネームタグ演出(任意)
- **除外リスト**: 特定Mob(村人、ゴーレム等)やボスを強化対象から除外するタグ設定
- **バランス調整**: 難易度が上がりすぎた場合のため、体力・ダメージ倍率など既存のvanilla難易度設定との組み合わせ確認
