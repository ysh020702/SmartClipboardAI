package com.samsung.smartclipboard.presentation.agent

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samsung.smartclipboard.ui.theme.AppColors
import com.samsung.smartclipboard.ui.theme.BlueGradient

@Composable
fun TopicInputScreen(
    topicQuery: String,
    isLoading: Boolean,
    errorMessage: String?,
    onTopicQueryChange: (String) -> Unit,
    onStart: () -> Unit
) {
    /*
    TODO:
    <데이터 클러스터링 / 주제 선정>
    1. AI 클러스터링 흐름 -> 주제 선정하기 컴포넌트 / 아래쪽에 있음
    1-1. 주제 리스트가 주루룩 표시. 한번 생성시키면 나타나는 리스트. 해당 리스트 중에 추천 주제 클릭하면 입력 텍스트가 자동으로 바뀜
    -> 이 AI는 주제만 선정함

    <데이터 선택>
    2. AI가 골라주기 / 직접 선택하기 버튼
    2-1. 데이터 선택 흐름으로 이어짐
    2-2. 둘다 똑같은 화면, 그러나 AI 가 골라주기 버튼은 AI 가 필요하다고 생각한 DataItem이 이미 선택되어있음.
    2-3. 이 화면에서 사용자가 DataItem을 추가적으로 선택하거나 편집할 수 있음
    2-4. 다 골랐으면 다음으로 넘어감.
    -> 이 AI는 주제에 맞는 DataItem만 선택함. 사용자가 편집할 수 있음

    <주제 & 데이터로 할 일 선택>
    3-1. AI 가 주제와 데이터로 어떤 작업을 할 수 있는지 결정함(Samsung Note에 메모, Calender에 등록, 내용을 공유)
    3-2. AI 가 각 작업마다 필요한 내용을 생성함.
    3-3. 사용자가 어떤 작업을 수행할 지 선택하고 생성한 내용을 확인함
        -> 가능한 작업 리스트 화면을 보여주고, 리스트의 세부 내용을 클릭하면, 세부내용 화면으로 들어감.
        -> 세부내용 화면에서는, 작업에 맞는 내용들이 편집 가능하게 표시됨. 노트면 텍스트 편집기 / 캘린더면 일정 선택, 일정 세부내용 수정 등
        -> 각 세부내용 화면에서, AI 제안 기능을 추가함. ~~ 한 내용을 추가할까요? 인데, 플로팅 버튼 느낌이면 좋을 것 같음.

        -> LLM 을 사용해서 내용을 추가할 수도 있고, 데이터로 생성한 부분 중에서 부족한 부분을 알려줘서 사용자가 편집할 수도 있어야 함.
            => 이 UX 세부설계 필요함!! 지금까지 구현 한 거 엎고 만들 거 같음.

    3-4. 세부 내용 화면에서는 사용자가 각 내용을 챗봇 AI를 활용해서 편집하거나, 직접 수동으로 수정함.
    3-5. 저장하기 버튼을 누르면 앱에 지금까지 만든 내용이 앱 내부 DB에 저장됨. / 다시 불러오거나 편집할 수 있게.
    3-6. 수행하기 버튼을 누르면 다른 앱으로 연결되어서 생성한 작업이 실행됨. / 이 실행이 중복으로 되는 것은 고려하지 않음



    <메인화면 재구성>
    1. AI 에이전트 진입점. 주제 입력 및 분석 세션으로 들어감. -> 2번의 DataItem이 얼마나 들어가있는지도 간략한 요약 정보를 보여줘야 함.
        -> 현재는 스크린샷 권한 요청 / 30개 자동 수집 기능도 1번에 같이 들어가 있어서 이 부분을 어디로 옮길지 봐야 함.
    2. DataItem 관리 화면. 어떤 데이터가 수집되어 앱에 저장되었는지 보여줌.
    3. 주제 관리 화면. <수정 필요!!>
        => 어떤 주제가 있었는지
            => 주제별로 어떤 데이터가 저장되었었는지 확인하고 편집
                => 추가/삭제하기 누르면 주제별 데이터 선택하기 화면으로 넘어가야 함.
                    => 거기서 데이터 편집하고, 작업 재생성하기를 수행할 수 있음.
            => 주제별로 어떤 작업인지 그리고 작업의 세부내용이 생성되었었는지 확인하고 편집
                => 편집하기? 추가적으로 수행하기? 누르면 주제별 작업 리스트로 들어가야 함.
                => 생각 멈춤. 도와줘.


     번외. 주제 => 작업 리스트 => 작업 세부내용 으로 계층적으로 이어지는 DB 조회 및 편집 구조를 설계해야 함.
     => 주제의 데이터 선택(DB)에서는 주제(Topic) - 데이터(DataItem) 다대다 연결 테이블이 필요함.
        => AI 가 선택하기...기능을 어떻게 구현할 지 모르겠네. 프롬포트에 주제 넣고 RAG 시스템처럼 구성한 다음에, DataItemEntitiy 의 ID를 뽑아와야 하나?
        => 사람이 선택하기 에서는 그 다대다 연결 테이블에 삽입/삭제만 수행하면 되니 간단.
     => 주제의 작업 선택(DB)에서는 주제(Topic) - 작업(??) 의 일대다 테이블이 필요함. 작업데이터 테이블에 어떤 주제로부터 왔는지 주제 ID를 저장하면 될 것.
     */


    var dashboardMode by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }

    val compactHeader = showDetails && !dashboardMode

    val arrowRotation by animateFloatAsState(
        targetValue = if (showDetails) 180f else 90f,
        animationSpec = tween(durationMillis = 250),
        label = "detailsArrowRotation"
    )

    val topPadding by animateDpAsState(
        targetValue = if (showDetails || dashboardMode) 20.dp else 48.dp,
        animationSpec = tween(durationMillis = 300),
        label = "topPadding"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(
                animationSpec = tween(durationMillis = 300)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (showDetails || dashboardMode) {
                        BlueGradient
                    } else {
                        SolidColor(Color.White)
                    }
                )
                .padding(horizontal = 20.dp)
                .padding(
                    top = topPadding,
                    bottom = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedContent(
                targetState = compactHeader,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith
                            fadeOut(animationSpec = tween(180)) using
                            SizeTransform(clip = false)
                },
                label = "headerModeTransition"
            ) { isCompact ->
                if (isCompact) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(
                                "SmartClipboardAI",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (dashboardMode) Color.White else AppColors.Blue,
                            modifier = Modifier.size(if (dashboardMode) 28.dp else 54.dp),
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "SmartClipboardAI",
                            fontSize = if (dashboardMode) 18.sp else 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (dashboardMode) Color.White else AppColors.Slate800,
                        )

                        Text(
                            "수집한 정보를 AI로 정리하고 실행합니다",
                            fontSize = 12.sp,
                            color = if (dashboardMode) {
                                Color.White.copy(alpha = 0.72f)
                            } else {
                                AppColors.Slate400
                            },
                        )

                        Spacer(Modifier.height(if (dashboardMode) 16.dp else 36.dp))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (showDetails) {
                        Color.White.copy(alpha = 0.10f)
                    } else {
                        Color.Transparent
                    }
                ),
                shape = RoundedCornerShape(20.dp),
                border = if (showDetails) {
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                } else {
                    null
                },
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            if (showDetails) {
                                SolidColor(Color.Transparent)
                            } else {
                                BlueGradient
                            }
                        )
                        .padding(20.dp),
                ) {
                    Text(
                        "무엇을 정리할까요?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "원하는 주제를 입력하거나 AI에게 맡겨보세요.",
                        color = Color(0xFFC7D2FE).copy(alpha = 0.78f),
                        fontSize = 11.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    TextField(
                        value = topicQuery,
                        onValueChange = onTopicQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "예: 회의 자료, 여행 링크, 일정 캡처",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.14f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.14f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                        ),
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                dashboardMode = true
                                showDetails = false
                            },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = AppColors.Blue
                            ),
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(Modifier.width(6.dp))

                            Text(
                                "AI 찾기",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onStart,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.18f),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                        ) {
                            Text("직접 고르기", fontSize = 13.sp)
                        }
                    }

                    if (!dashboardMode) {
                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = { showDetails = !showDetails },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.08f),
                                contentColor = Color(0xFFC7D2FE)
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.rotate(arrowRotation),
                            )

                            Spacer(Modifier.width(4.dp))

                            Text(
                                if (showDetails) "수집 현황 닫기" else "수집 현황 보기",
                                fontSize = 12.sp
                            )

                            AnimatedVisibility(
                                visible = !showDetails,
                                enter = fadeIn(animationSpec = tween(200)),
                                exit = fadeOut(animationSpec = tween(100))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(Modifier.width(6.dp))

                                    Pill(
                                        "5개",
                                        bg = Color.White.copy(alpha = 0.14f),
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !dashboardMode && !showDetails,
            enter = fadeIn(animationSpec = tween(250)) +
                    expandVertically(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(150)) +
                    shrinkVertically(animationSpec = tween(150))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DividerLine(Modifier.weight(1f))

                Text(
                    "오늘 5개의 항목을 수집했어요",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = AppColors.Slate400,
                    fontSize = 10.sp
                )

                DividerLine(Modifier.weight(1f))
            }
        }

        AnimatedVisibility(
            visible = showDetails || dashboardMode,
            enter = fadeIn(animationSpec = tween(250)) +
                    expandVertically(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(150)) +
                    shrinkVertically(animationSpec = tween(200))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("테스트 화면입니다.")
            }
        }
    }
}

@Composable
private fun Pill(text: String, bg: Color, color: Color) {
    Box(Modifier.clip(CircleShape).background(bg).padding(horizontal = 7.dp, vertical = 3.dp)) {
        Text(text, color = color, fontSize = 9.sp, maxLines = 1)
    }
}
@Composable
private fun DividerLine(modifier: Modifier = Modifier) {
    Box(modifier.height(1.dp).background(AppColors.Slate200))
}
