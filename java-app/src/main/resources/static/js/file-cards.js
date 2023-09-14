$(document).ready(function() {
  const container = $('#cardContainer');
  const maxCards = 5;  // 스크롤을 적용할 카드 개수

  function checkCardCount() {
    const cardCount = container.find('.card').length;
    if (cardCount > maxCards) {
      container.addClass('scrollable');
    } else {
      container.removeClass('scrollable');
    }
  }

  // 초기 카드 개수 확인
  checkCardCount();

  // 새로운 카드가 추가될 때마다 함수 호출 (예시)
  // 새로운 카드 추가 로직에 따라 이 부분을 적절히 수정해야 함
  container.on('cardAdded', function() {
    checkCardCount();
  });

  const utcTimeElements = document.querySelectorAll('.utc-time');
  const localTimeElements = document.querySelectorAll('.local-time');
  
  utcTimeElements.forEach((utcTimeElement, index) => {
      const utcTime = new Date(utcTimeElement.textContent);

      // 날짜와 시간을 원하는 형식으로 포맷
      const yyyy = utcTime.getFullYear();
      const MM = String(utcTime.getMonth() + 1).padStart(2, '0'); // 1월은 0이므로 +1을 해주고, 두 자릿수를 유지
      const dd = String(utcTime.getDate()).padStart(2, '0');
      const hh = String(utcTime.getHours()).padStart(2, '0');
      const mm = String(utcTime.getMinutes()).padStart(2, '0');
      const ss = String(utcTime.getSeconds()).padStart(2, '0');

      const formattedTime = `${yyyy}${MM}${dd} ${hh}:${mm}:${ss}`;

      // 변환된 시간을 페이지에 표시
      localTimeElements[index].textContent = formattedTime;
      utcTimeElement.style.display = 'none'; // UTC 시간을 숨깁니다.
  });
});