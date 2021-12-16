# Copyright 2021 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

desc "Run Link checks"

flag :install, desc: "Install linkinator instead of running checks"

include :exec, e: true
include :terminal

def run
  ::Dir.chdir context_directory
  if install
    Kernel.exec "npm install linkinator"
  else
    exec_tool ["yardoc"]
    check_links
  end
end

def check_links
  result = exec ["npx", "linkinator", "./doc"], out: :capture
  puts result.captured_out
  checked_links = result.captured_out.split "\n"
  checked_links.select! { |link| link =~ /^\[(\d+)\]/ && ::Regexp.last_match[1] != "200" }
  unless checked_links.empty?
    checked_links.each do |link|
      puts link, :yellow
    end
    exit 1
  end
end
