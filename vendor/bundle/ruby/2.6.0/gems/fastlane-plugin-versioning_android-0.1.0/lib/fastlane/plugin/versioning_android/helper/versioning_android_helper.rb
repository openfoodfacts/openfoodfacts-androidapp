module Fastlane
  module Helper
    class VersioningAndroidHelper
      require "shellwords"
      require "tempfile"
      require "fileutils"

      GRADLE_FILE_TEST = "/tmp/fastlane/tests/versioning/app/build.gradle"

      def self.get_gradle_file(gradle_file)
        return Helper.test? ? GRADLE_FILE_TEST : gradle_file
      end

      def self.get_gradle_file_path(gradle_file)
        gradle_file = self.get_gradle_file(gradle_file)
        return File.expand_path(gradle_file).shellescape
      end

      def self.get_new_version_code(gradle_file, new_version_code)
        if new_version_code.nil?
          current_version_code = self.read_key_from_gradle_file(gradle_file, "versionCode")
          new_version_code = current_version_code.to_i + 1
        end

        return new_version_code.to_i
      end

      def self.get_new_version_name(gradle_file, new_version_name, bump_type = nil)
        if new_version_name.nil?
          current_version_name = self.read_key_from_gradle_file(gradle_file, "versionName")
          current_version_parts = current_version_name.split(/[.]/)

          major = current_version_parts[0].to_i
          minor = current_version_parts[1].to_i
          patch = current_version_parts[2].to_i

          if bump_type == "major"
            new_version_name = "#{major + 1}.0.0"
          elsif bump_type == "minor"
            new_version_name = "#{major}.#{minor + 1}.0"
          elsif bump_type == "patch"
            new_version_name = "#{major}.#{minor}.#{patch + 1}"
          end
        end

        return new_version_name.to_s
      end

      def self.read_key_from_gradle_file(gradle_file, key)
        value = false
        begin
          file = File.new(gradle_file, "r")
          while (line = file.gets)
            next unless line.include? key
            components = line.strip.split(' ')
            value = components[components.length - 1].tr("\"", "")
            break
          end
          file.close
        rescue => err
          UI.error("An exception occured while reading gradle file: #{err}")
          err
        end
        return value
      end

      def self.save_key_to_gradle_file(gradle_file, key, value)
        current_value = self.read_key_from_gradle_file(gradle_file, key)

        begin
          found = false
          temp_file = Tempfile.new("flSave_#{key}_ToGradleFile")
          File.open(gradle_file, "r") do |file|
            file.each_line do |line|
              if line.include? key and found == false
                found = true
                line.replace line.sub(current_value.to_s, value.to_s)
              end
              temp_file.puts line
            end
            file.close
          end
          temp_file.rewind
          temp_file.close
          FileUtils.mv(temp_file.path, gradle_file)
          temp_file.unlink
        end

        return found == true ? value : -1
      end
    end
  end
end
