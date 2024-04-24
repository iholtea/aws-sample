
/* Bootstrap breakpoints 576<sm<768  768<md<992
$grid-breakpoints: (
  xs: 0,
  sm: 576px,
  md: 768px,
  lg: 992px,
  xl: 1200px,
  xxl: 1400px
);
*/

// local SpringBoot REST backend 
const baseUrl = 'http://localhost:8080';
const apiKey = 'dummyKey';

// manual created API Gateway in AWS web console
//const baseUrl = 'https://95k6wo08hg.execute-api.us-east-1.amazonaws.com/test/todos'
//const apiKey = 'AGdASLxD8t3chg1F6uCe251G2vFWrdQt6ZgUnP8K';

// AWS CDK created API Gateway
//const baseUrl = 'https://nrjp1jpvc8.execute-api.us-east-1.amazonaws.com/test/todos'
//const apiKey = 'RZluarPnz29wgpnsdJ4kya74bJbI1DaDanjWKVa6';

const todoUrl = baseUrl + '/todos';
const loginUrl = baseUrl + '/login';
const registerUrl = baseUrl + '/register'

const apiKeyHeader = 'x-api-key';

const authHeader = 'authorization';
const authHeaderPrefix = 'Bearer ';

let jwt = '';

let todos = new Map();
let currentTodoUuid = null;
let currentItemUuid = null;

function clearCachedData() {
  todos = new Map();
  currentTodoUuid = null;
  currentItemUuid = null; 
}

export default {
  apiKeyHeader,
  baseUrl,
  todoUrl,
  loginUrl,
  registerUrl,
  apiKey,
  todos,
  currentTodoUuid,
  currentItemUuid,
  jwt,
  authHeader,
  authHeaderPrefix,
  clearCachedData
}
