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

mixin "kokoro-tools" do
  def load_env
    return if defined? @loaded_env

    service_account = "#{::ENV['KOKORO_GFILE_DIR']}/service-account.json"
    raise "#{service_account} is not a file" unless ::File.file? service_account
    ::ENV["GOOGLE_APPLICATION_CREDENTIALS"] = service_account

    filename = "#{::ENV['KOKORO_GFILE_DIR']}/ruby_env_vars.json"
    raise "#{filename} is not a file" unless ::File.file? filename
    env_vars = ::JSON.parse ::File.read filename
    env_vars.each { |k, v| ::ENV[k] = v }

    ::ENV["DOCS_CREDENTIALS"] ||= "#{::ENV['KOKORO_KEYSTORE_DIR']}/73713_docuploader_service_account"
    ::ENV["GITHUB_TOKEN"] ||= "#{::ENV['KOKORO_KEYSTORE_DIR']}/73713_yoshi-automation-github-key"

    @loaded_env = true
  end

  def package_name
    @package_name ||=
      ::ENV["RELEASE_PACKAGE"] || ::ENV["PACKAGE"] || begin
        files = ::Dir.glob("*.gemspec")
        raise "Unable to determine package" unless files.length == 1
        ::File.basename files.first, ".gemspec"
      end
  end

  def package_directory
    @package_directory ||= begin
      if ::File.file? "#{package_name}.gemspec"
        ::File.expand_path "."
      elsif ::File.file? "#{package_name}/#{package_name}.gemspec"
        ::File.expand_path package_name
      else
        raise "Unable to determine package directory"
      end
    end
  end

  def package_gemspec_path
    @package_gemspec_path ||= ::File.join package_directory, "#{package_name}.gemspec"
  end

  def package_gemspec
    @package_gemspec ||= eval ::File.read package_gemspec_path
  end

  def package_version
    @package_version ||= package_gemspec.version
  end
end
