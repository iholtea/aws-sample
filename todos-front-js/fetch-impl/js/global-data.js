const apiKeyHeader = 'x-api-key';

// local SpringBoot REST backend 
const baseUrl = 'http://localhost:8080/todos';
const apiKey = 'dummyKey';

const todos = new Map();

let currentTodoUuid = null;
let currentItemUuid = null;

export default {
  apiKeyHeader,
  baseUrl,
  apiKey,
  todos,
  currentTodoUuid,
  currentItemUuid
}
