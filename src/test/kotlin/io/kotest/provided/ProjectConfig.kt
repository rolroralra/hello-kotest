package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.spring.SpringExtension
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Kotest 6 는 `io.kotest.provided.ProjectConfig` 라는 이름의 클래스를 자동으로 찾아 프로젝트 전역 설정으로 사용한다.
 * (13-advanced-config.md 참고)
 */
class ProjectConfig : AbstractProjectConfig() {

    // 모든 Spec 에 SpringExtension 을 적용한다. @SpringBootTest 등이 붙은 Spec 에서만 실제로 동작한다.
    override val extensions: List<Extension> = listOf(SpringExtension())

    // 테스트 하나가 이 시간을 넘기면 실패로 처리한다.
    override val timeout: Duration = 30.seconds
}
