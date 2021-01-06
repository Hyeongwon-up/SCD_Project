# SCD_Project
### SCDBenchmark

`DiffExample`: SCD tool 사용 예시 (GumTree, CLDiff, LAS).  
`DiffExample2`: SCD tool 사용 예시 (ChangeDistiller).

ChangeDistiller를 실행시킬 때는 라이브러리 충돌 때문에 classpath 설정에 changedistiller-xxx.jar 파일을 최상단(Top)으로 올려줘야함.  
반대로 다른 것들을 실행할 때는 해당 파일을 최하단(Bottom)으로 내려줘야함.  
Eclipse에서 Configure Build Path > Order and Export > changedistiller-xxx.jar 선택 후 오른쪽의 Top 또는 Bottom 버튼을 누르면 됨.  

`IdentifyChangedFiles`: Subject에서 변경된 파일정보를 수집하는 부분.   
ChangeDistiller실행을 분리할 수 밖에 없으므로 미리 파일정보들을 가져와 반복해서 쓰는게 효율적임.  
또 미리 얻은 정보로 변경된 파일 수가 너무 적거나 많으면 건너뛰도록 설정 가능.  

`CollectChanges`: 변경된 파일들 정보를 이용하여 실제 SCD tool을 적용하여 변경사항을 추출하고 DB에 저장.  


### Subject 준비

현재 subject로는 4개의 오픈소스 프로젝트를 사용.  

Apache commons collections: https://github.com/apache/commons-collections    
Apache commons lang: https://github.com/apache/commons-lang  
Apache commons math: https://github.com/apache/commons-math  
Apache ant ivy: https://github.com/apache/ant-ivy 

다음의 형태로 subject들을 가져옴.  

1. `BaseDir`을 정하고, 그 밑에 `old` 디렉토리를 생성.  
2. `old`로 이동하여 4개의 저장소를 모두 clone - e.g.) `git clone https://github.com/apache/ant-ivy ivy` 
3. Clone시 디렉토리명을 프로젝트 이름 마지막 단어로 해주는 것에 주의할 것 (코드에 지정한 프로젝트 이름과 일치시키기 위함).  
4. `old`의 내용을 `new`로 복사.  

### DB테이블 생성 및 접속정보 설정

db.properties에 접속정보를 입력하고, 프로그램의 쿼리에 맞춰 테이블을 생성 또는 쿼리를 변경.

### 프로그램 실행 전 설정

1. baseDir에 subject가 있는 디렉토리를 입력.
2. `int tool`에 사용할 SCD tool을 입력 - `GUMTREE` 또는 `CHANGE_DISTILLER`. 현재는 2개만 사용.
3. SCD tool에 맞춰 classpath 설정 - changedistiller-*.jar를 빼거나 집어넣기.
4. `insertFileInfo`로 파일정보들도 DB에 넣을 것인지 결정 - 처음 한 번만 넣으면 됨.
5. SCD tool사이에 출력이 일치하도록 적절히 수정하는 부분을 넣고 프로그램을 실행.


### 출력 일치를 위해 확인할 부분

Change Type과 Changed Entity Type을 저장하게 되어 있는데, 이 두 가지만 잘 일치하도록 변경하면 됨.  
`DiffExample`과 `DiffExample2`를 반복해서 실행하면서 GumTree와 ChangeDistiller의 출력 차이를 적절히 수정할 코드를 만들면 됨.  
