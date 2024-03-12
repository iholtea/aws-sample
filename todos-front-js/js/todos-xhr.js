const baseUrl = 'http://localhost:8080/todos';
//const baseUrl = 'https://95k6wo08hg.execute-api.us-east-1.amazonaws.com/test/todos'

/*
* I guess I could create a successCallback and an errorCallback
*/
function  fetchAllLists(callback) {
  const xhr = new XMLHttpRequest();
  xhr.open('GET', baseUrl, true);
  xhr.onload = function() {
    if(xhr.status === 200) {
      callback(null, xhr.responseText);
    } else {
      console.log(`todosXhr.fetchAllLists http status error ${xhr.status}`);
      callback('ERR_HTTP_STATUS', xhr.responseText, xhr.status);
    }
  }
  xhr.onerror = function() {
    console.log('todosXhr.fetchAllLists generic error');
    // do we need to pass the null argument ? wouldn't it be undefined if not provided
    callback('ERR_OTHER');
  }
  xhr.send();
}

function fetchListById(uuid, callback) {
  const listUrl = `${baseUrl}/${uuid}`;
  const xhr = new XMLHttpRequest();
  xhr.open('GET', listUrl, true);
  xhr.onload = function() {
    if(xhr.status === 200) {
      callback(null, xhr.responseText);
    } else {
        callback('ERR_LOAD', xhr.status);
    }
  }
  xhr.onerror = function() {
      callback( 'ERR_OTHER', null );
  }
  xhr.send();
}

export default {
  fetchAllLists,
  fetchListById  
}

